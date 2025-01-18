package test;

import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import com.mrcrayfish.controllable.client.binding.context.GlobalContext;
import com.mrcrayfish.controllable.client.binding.context.InGameContext;
import com.mrcrayfish.controllable.client.binding.context.InGameWithScreenContext;
import com.mrcrayfish.controllable.client.binding.context.InScreenContext;
import com.mrcrayfish.controllable.client.binding.context.rule.ContextRule;
import com.mrcrayfish.controllable.client.binding.context.rule.HasPlayerRule;
import com.mrcrayfish.controllable.client.binding.context.rule.HasScreenRule;
import com.mrcrayfish.controllable.client.binding.context.rule.NoScreenRule;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Author: MrCrayfish
 */
public class ScreenRuleTest
{
    @Test
    public void testScreenRule()
    {
        // These should not be equal one rule wants any screen present, while the other wants nothing
        assertTrue(NoScreenRule.get().matches(NoScreenRule.get()));
    }

    @Test
    public void testScreenRuleWithScreenAgainstAny()
    {
        // These should be equal since one rule accepts all screens and anvil screen will conflict
        assertTrue(HasScreenRule.of(Set.of(AnvilScreen.class)).matches(HasScreenRule.any()));
    }

    @Test
    public void testScreenRuleWithDifferentScreens()
    {
        // These should be not equal since they accept different screens
        assertFalse(HasScreenRule.of(Set.of(LoomScreen.class)).matches(HasScreenRule.of(Set.of(AnvilScreen.class))));
    }

    @Test
    public void testScreenRuleWithCommonScreen()
    {
        // These should be equal since the second rule also has loom screen
        assertTrue(HasScreenRule.of(Set.of(LoomScreen.class)).matches(HasScreenRule.of(Set.of(AnvilScreen.class, LoomScreen.class))));
    }

    @Test
    public void testBindingContextConflictWithSingleRule()
    {
        BindingContext contextA = new InGameContext(Utils.resource("context_a")) {
            @Override
            public Set<ContextRule> createRules() {
                return Set.of(HasScreenRule.any());
            }
        };
        BindingContext contextB = new InGameContext(Utils.resource("context_b")) {
            @Override
            public Set<ContextRule> createRules() {
                return Set.of(HasScreenRule.of(Set.of(AnvilScreen.class)));
            }
        };
        // This should be true since contextA is asking for any screen, while contextB is asking for
        // the anvil screen. any screen == anvil screen
        assertTrue(contextA.conflicts(contextB));
    }

    @Test
    public void testBindingContextConflictWithMultipleRules()
    {
        BindingContext contextC = new InGameContext(Utils.resource("context_c")) {
            @Override
            public Set<ContextRule> createRules() {
                return Set.of(HasPlayerRule.get(), HasScreenRule.of(Set.of(CraftingScreen.class, AnvilScreen.class)));
            }
        };
        BindingContext contextD = new InGameContext(Utils.resource("context_d")) {
            @Override
            public Set<ContextRule> createRules() {
                return Set.of(HasScreenRule.of(Set.of(AnvilScreen.class)));
            }
        };
        // This should be true since both ask for a player, and both will be contexts are active
        // when in an anvil screen
        assertTrue(contextC.conflicts(contextD));
    }

    @Test
    public void testBindingContextNoConflictWithMultipleRules()
    {
        BindingContext contextE = new InGameContext(Utils.resource("context_e")) {
            @Override
            public Set<ContextRule> createRules() {
                return Set.of(HasScreenRule.of(Set.of(CraftingScreen.class)));
            }
        };
        BindingContext contextF = new InGameContext(Utils.resource("context_f")) {
            @Override
            public Set<ContextRule> createRules() {
                return Set.of(HasPlayerRule.get(), HasScreenRule.of(Set.of(AnvilScreen.class)));
            }
        };
        // This should be false. Both ask for a player, however they target different screens. So
        // the contexts will never clash.
        assertFalse(contextE.conflicts(contextF));
    }

    @Test
    public void testBuiltinBindingContextConflicts()
    {
        // Should be true as global conflicts with everything
        assertTrue(GlobalContext.INSTANCE.conflicts(InGameContext.INSTANCE));

        // Should be false as "in game" wants no screen while "in screen" wants a screen
        assertFalse(InGameContext.INSTANCE.conflicts(InScreenContext.INSTANCE));

        // Should be false as "in game" wants no screen while "in game with screen" wants a screen
        assertFalse(InGameContext.INSTANCE.conflicts(InGameWithScreenContext.INSTANCE));

        // Should be true as "in game with screen" wants a screen while "in screen" also wants a screen
        assertTrue(InGameWithScreenContext.INSTANCE.conflicts(InScreenContext.INSTANCE));
    }
}
