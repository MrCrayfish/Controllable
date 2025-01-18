package com.mrcrayfish.controllable.client.binding.context;

import com.google.common.base.Preconditions;
import com.mrcrayfish.controllable.client.binding.context.rule.ContextRule;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The base class for creating binding contexts. Binding contexts determine if a button binding is
 * able to be handled during a state in the game. For example, a context can be made for a button
 * binding to only work while a screen is open, or when a player is riding an entity.
 * <p>
 * To create a custom context, extend one of the following:
 * <pre>
 * - {@link GlobalContext}
 * - {@link InGameContext}
 * - {@link InScreenContext}
 * </pre>
 * Author: MrCrayfish
 */
public abstract class BindingContext
{
    private static final Set<ResourceLocation> REGISTERED_CONTEXTS = Collections.synchronizedSet(new HashSet<>());
    private static final Map<ConflictKey, Boolean> CONFLICT_CACHE = new HashMap<>();

    private final ResourceLocation id;
    private Set<ContextRule> rules;

    BindingContext(ResourceLocation id)
    {
        Preconditions.checkState(REGISTERED_CONTEXTS.add(id), "Duplicate binding context id: %s".formatted(id));
        this.id = id;
    }

    /**
     * Factory method that creates the rules for this binding context. You can find available rules
     * in {@link com.mrcrayfish.controllable.api.client.binding.context.rule} and use their helper
     * methods to get an instance.
     *
     * @return
     */
    protected abstract Set<ContextRule> createRules();

    /**
     * @return The rules for this binding context
     */
    public final Set<ContextRule> rules()
    {
        if(this.rules == null)
        {
            this.rules = this.createRules();
        }
        return this.rules;
    }

    /**
     * @return A boolean that represents if this context is active
     */
    public final boolean isActive()
    {
        Set<ContextRule> rules = this.rules();
        return rules.isEmpty() || rules.stream().allMatch(ContextRule::isActive);
    }

    /**
     * Determines if this context conflicts with another context. While this
     * <p>
     * If the context conflicts, as determined by {@link #conflicts(BindingContext)}, but the
     * priority is different
     *
     * @param other
     * @return True if this context conflicts with another context
     */
    public final boolean conflicts(BindingContext other)
    {
        return CONFLICT_CACHE.computeIfAbsent(ConflictKey.of(this, other), key -> {
            // If priorities are different, they will never conflict
            if(this.priority() != other.priority())
                return false;
            return doAllRulesMatch(this.rules(), other.rules());
        });
    }

    /**
     * The priority of this context. A higher priority means that a button binding will be handled
     * first before button bindings with a lower priority.
     *
     * @return An int representing the priority
     */
    public abstract int priority();

    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }

    @Override
    public final boolean equals(Object o)
    {
        if(!(o instanceof BindingContext that))
            return false;
        return this.id.equals(that.id);
    }

    public record ConflictKey(BindingContext first, BindingContext second)
    {
        @Override
        public boolean equals(Object o)
        {
            if(!(o instanceof ConflictKey(BindingContext a, BindingContext b)))
                return false;
            return Objects.equals(this.first, a) && Objects.equals(this.second, b);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.first, this.second);
        }

        public static ConflictKey of(BindingContext first, BindingContext second)
        {
            // Ensures the correctness of the field order for the key
            int result = first.id.compareTo(second.id);
            return new ConflictKey(result >= 0 ? first : second, result >= 0 ? second : first);
        }
    }

    private static boolean doAllRulesMatch(Set<ContextRule> a, Set<ContextRule> b)
    {
        return (a.isEmpty() || b.isEmpty())
            || a.stream().allMatch(r1 -> b.stream().anyMatch(r2 -> r2.matches(r1)))
            || b.stream().allMatch(r1 -> a.stream().anyMatch(r2 -> r2.matches(r1)));
    }
}
