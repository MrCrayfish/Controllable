package com.mrcrayfish.controllable.asm;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class ControllableTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if(bytes == null)
        {
            return null;
        }

        switch(transformedName)
        {
            case "net.minecraft.client.Minecraft":
                bytes = this.patch(new MethodEntry("func_147115_a", "sendClickBlockToController", "(Z)V"), this::patch_Minecraft_sendClickBlockToController, bytes);
                bytes = this.patch(new MethodEntry("func_184117_aA", "processKeyBinds", "()V"), this::patch_Minecraft_processKeyBinds, bytes);
                break;
            case "net.minecraft.client.gui.inventory.GuiContainer":
                bytes = this.patch(new MethodEntry("func_73864_a", "mouseClicked", "(III)V"), this::patch_GuiContainer_mouseClicked, bytes);
                bytes = this.patch(new MethodEntry("func_146286_b", "mouseReleased", "(III)V"), this::patch_GuiContainer_mouseReleased, bytes);
                break;
            case "net.minecraftforge.client.GuiIngameForge":
                bytes = this.patch(new MethodEntry("", "renderPlayerList", "(II)V"), this::patch_GuiIngameForge_renderPlayerList, bytes);
                bytes = this.patch(new MethodEntry("", "renderRecordOverlay", "(IIF)V"), this::patch_IngameGui_renderSelectedItem, bytes);
                bytes = this.patch(new MethodEntry("", "renderToolHighlight", "(Lnet/minecraft/client/gui/ScaledResolution;)V"), this::patch_IngameGui_renderSelectedItem, bytes);
                break;
            case "net.minecraft.client.renderer.EntityRenderer":
                bytes = this.patch(new MethodEntry("func_181560_a", "updateCameraAndRender", "(FJ)V"), this::patch_EntityRenderer_updateCameraAndRender, bytes);
                break;
        }
        return bytes;
    }

    @Nullable
    private MethodNode findMethod(List<MethodNode> methods, MethodEntry entry)
    {
        for(MethodNode node : methods)
        {
            if((node.name.equals(entry.obfName) || node.name.equals(entry.name)) && node.desc.equals(entry.desc))
            {
                return node;
            }
        }
        return null;
    }

    private byte[] patch(MethodEntry entry, Function<MethodNode, Boolean> f, byte[] bytes)
    {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        MethodNode methodNode = this.findMethod(classNode.methods, entry);
        String className = classNode.name.replace("/", ".") + "#" + entry.name + entry.desc;
        if(methodNode != null)
        {
            Controllable.LOGGER.info("Starting to patch: " + className);
            if(f.apply(methodNode))
            {
                Controllable.LOGGER.info("Successfully patched: " + className);
            }
            else
            {
                Controllable.LOGGER.info("Failed to patch: " + className);
            }
        }
        else
        {
            Controllable.LOGGER.info("Failed to find method: " + className);
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private boolean patch_Minecraft_sendClickBlockToController(MethodNode methodNode)
    {
        methodNode.instructions.insert(new VarInsnNode(Opcodes.ISTORE, 1));
        methodNode.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "isLeftClicking", "()Z", false));
        return true;
    }

    private boolean patch_Minecraft_processKeyBinds(MethodNode methodNode)
    {
        MethodEntry entry = new MethodEntry("func_151470_d", "isKeyDown", "()Z");

        AbstractInsnNode foundNode = null;
        for(AbstractInsnNode node : methodNode.instructions.toArray())
        {
            if(node.getOpcode() == Opcodes.INVOKEVIRTUAL && node.getNext().getOpcode() == Opcodes.IFNE)
            {
                MethodInsnNode m = (MethodInsnNode) node;
                if((entry.obfName.equals(m.name) || entry.name.equals(m.name)) && entry.desc.equals(m.desc))
                {
                    if(node.getPrevious().getOpcode() == Opcodes.GETFIELD && node.getPrevious().getPrevious().getOpcode() == Opcodes.GETFIELD && node.getPrevious().getPrevious().getPrevious().getOpcode() == Opcodes.ALOAD)
                    {
                        foundNode = node;
                        break;
                    }
                }
            }
        }

        if(foundNode != null)
        {
            AbstractInsnNode nextNode = foundNode.getNext();
            if(!this.removeNthNodes(methodNode.instructions, foundNode, -3))
            {
                return false;
            }
            methodNode.instructions.remove(foundNode);
            methodNode.instructions.insertBefore(nextNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "isRightClicking", "()Z", false));
            return true;
        }
        return false;
    }

    private boolean patch_GuiContainer_mouseClicked(MethodNode methodNode)
    {
        return this.patchQuickMove(methodNode);
    }

    private boolean patch_GuiContainer_mouseReleased(MethodNode methodNode)
    {
        return this.patchQuickMove(methodNode);
    }

    private boolean patchQuickMove(MethodNode method)
    {
        MethodEntry entry = new MethodEntry("func_151470_d", "isKeyDown", "()Z");

        AbstractInsnNode foundNode = null;
        for(AbstractInsnNode node : method.instructions.toArray())
        {
            if(node.getOpcode() != Opcodes.INVOKESTATIC)
            {
                continue;
            }

            if(node.getNext() == null || node.getNext().getOpcode() != Opcodes.IFNE)
            {
                continue;
            }

            if(node.getPrevious() == null || node.getPrevious().getOpcode() != Opcodes.BIPUSH)
            {
                continue;
            }

            MethodInsnNode methodNode = (MethodInsnNode) node;
            if(!(entry.obfName.equals(methodNode.name) || entry.name.equals(methodNode.name)))
            {
                continue;
            }

            AbstractInsnNode temp = node.getNext().getNext().getNext();

            if(temp == null)
            {
                continue;
            }

            if(temp.getOpcode() != Opcodes.INVOKESTATIC && temp.getNext().getOpcode() != Opcodes.IFEQ && temp.getPrevious().getOpcode() != Opcodes.BIPUSH)
            {
                continue;
            }

            methodNode = (MethodInsnNode) temp;
            if(!(entry.obfName.equals(methodNode.name) || entry.name.equals(methodNode.name)))
            {
                continue;
            }

            foundNode = node.getPrevious();
        }

        if(foundNode != null)
        {
            AbstractInsnNode previous = foundNode.getPrevious();
            this.removeNthNodes(method.instructions, foundNode, 4);
            method.instructions.remove(foundNode);
            method.instructions.insert(previous, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "canQuickMove", "()Z", false));
            return true;
        }
        return false;
    }

    private boolean patch_GuiIngameForge_renderPlayerList(MethodNode method)
    {
        MethodEntry entry = new MethodEntry("func_151470_d", "isKeyDown", "()Z");
        AbstractInsnNode foundNode = null;
        for(AbstractInsnNode node : method.instructions.toArray())
        {
            if(node.getOpcode() != Opcodes.INVOKEVIRTUAL)
            {
                continue;
            }
            MethodInsnNode methodNode = (MethodInsnNode) node;
            if(!(entry.obfName.equals(methodNode.name) || entry.name.equals(methodNode.name)))
            {
                continue;
            }
            if(this.getNthRelativeNode(node, -1) == null || this.getNthRelativeNode(node, -1).getOpcode() != Opcodes.GETFIELD)
            {
                continue;
            }
            if(this.getNthRelativeNode(node, -2) == null || this.getNthRelativeNode(node, -2).getOpcode() != Opcodes.GETFIELD)
            {
                continue;
            }
            if(this.getNthRelativeNode(node, -3) == null || this.getNthRelativeNode(node, -3).getOpcode() != Opcodes.GETFIELD)
            {
                continue;
            }
            if(this.getNthRelativeNode(node, -4) == null || this.getNthRelativeNode(node, -4).getOpcode() != Opcodes.ALOAD)
            {
                continue;
            }
            foundNode = node;
            break;
        }

        if(foundNode != null)
        {
            if(!this.removeNthNodes(method.instructions, foundNode, -4))
                return false;
            method.instructions.insert(foundNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "canShowPlayerList", "()Z", false));
            method.instructions.remove(foundNode);
            return true;
        }
        return false;
    }

    private boolean patch_EntityRenderer_updateCameraAndRender(MethodNode method)
    {
        MethodEntry entry = new MethodEntry("", "drawScreen", "(Lnet/minecraft/client/gui/GuiScreen;IIF)V");
        AbstractInsnNode foundNode = null;
        for(AbstractInsnNode node : method.instructions.toArray())
        {
            if(node.getOpcode() != Opcodes.INVOKESTATIC)
                continue;
            MethodInsnNode methodNode = (MethodInsnNode) node;
            if(entry.name.equals(methodNode.name) && entry.desc.equals(methodNode.desc))
            {
                foundNode = node;
                break;
            }
        }

        if(foundNode != null)
        {
            AbstractInsnNode previousNode = foundNode.getPrevious();
            method.instructions.remove(foundNode);
            method.instructions.insert(previousNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "drawScreen", "(Lnet/minecraft/client/gui/GuiScreen;IIF)V", false));
            return true;
        }
        return false;
    }

    private boolean patch_IngameGui_renderSelectedItem(MethodNode method)
    {
        MethodEntry entry = new MethodEntry("func_179094_E", "pushMatrix", "()V");
        AbstractInsnNode foundNode = null;
        for(AbstractInsnNode node : method.instructions.toArray())
        {
            if(node.getOpcode() != Opcodes.INVOKESTATIC)
                continue;
            MethodInsnNode methodNode = (MethodInsnNode) node;
            if((entry.obfName.equals(methodNode.name) || entry.name.equals(methodNode.name)) && entry.desc.equals(methodNode.desc))
            {
                foundNode = node;
                break;
            }
        }
        if(foundNode != null)
        {
            method.instructions.insert(foundNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "applyHotbarOffset", "()V", false));
            return true;
        }
        return false;
    }

    private boolean removeNthNodes(InsnList instructions, AbstractInsnNode node, int n)
    {
        while(n > 0)
        {
            if(node.getNext() == null)
            {
                return false;
            }
            instructions.remove(node.getNext());
            n--;
        }
        while(n < 0)
        {
            if(node.getPrevious() == null)
            {
                return false;
            }
            instructions.remove(node.getPrevious());
            n++;
        }
        return true;
    }

    private AbstractInsnNode getNthRelativeNode(AbstractInsnNode node, int n)
    {
        while(n > 0 && node != null)
        {
            node = node.getNext();
            n--;
        }
        while(n < 0 && node != null)
        {
            node = node.getPrevious();
            n++;
        }
        return node;
    }

    public static class MethodEntry
    {
        private String obfName;
        private String name;
        private String desc;

        public MethodEntry(String obfName, String name, String desc)
        {
            this.obfName = obfName;
            this.name = name;
            this.desc = desc;
        }
    }
}
