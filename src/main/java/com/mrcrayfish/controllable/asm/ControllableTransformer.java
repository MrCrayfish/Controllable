package com.mrcrayfish.controllable.asm;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Author: MrCrayfish
 */
public class ControllableTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if(bytes == null)
            return null;

        //boolean isObfuscated = !name.equals(transformedName);
        if(transformedName.equals("net.minecraft.client.Minecraft"))
        {
            return patchMinecraft(bytes);
        }
        else if(transformedName.equals("net.minecraft.client.gui.inventory.GuiContainer"))
        {
            return patchGuiContainer(bytes);
        }
        return bytes;
    }

    private byte[] patchMinecraft(byte[] bytes)
    {
        Controllable.LOGGER.info("Patching net.minecraft.client.Minecraft");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        ObfName method_sendClickBlockToController = new ObfName("func_147115_a", "sendClickBlockToController");
        String params_sendClickBlockToController = "(Z)V";

        ObfName method_processKeyBinds = new ObfName("func_184117_aA", "processKeyBinds");
        String params_processKeyBinds = "()V";

        for(MethodNode method : classNode.methods)
        {
            if(method_sendClickBlockToController.equals(method.name) && method.desc.equals(params_sendClickBlockToController))
            {
                Controllable.LOGGER.info("Patching #sendClickBlockToController");

                method.instructions.insert(new VarInsnNode(Opcodes.ISTORE, 1));
                method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/ControllerInput", "isLeftClicking", "()Z", false));

                Controllable.LOGGER.info("Successfully patched #sendClickBlockToController");
            }
            else if(method_processKeyBinds.equals(method.name) && method.desc.equals(params_processKeyBinds))
            {
                Controllable.LOGGER.info("Patching #processKeyBinds");

                //ObfName method_onStoppedUsingItem = new ObfName("func_78766_c", "onStoppedUsingItem");
                ObfName method_isKeyDown = new ObfName("func_151470_d", "isKeyDown");
                String params_isKeyDown = "()Z";

                AbstractInsnNode foundNode = null;
                for(AbstractInsnNode node : method.instructions.toArray())
                {
                    if(node.getOpcode() == Opcodes.INVOKEVIRTUAL && node.getNext().getOpcode() == Opcodes.IFNE)
                    {
                        if(node instanceof MethodInsnNode && method_isKeyDown.equals(((MethodInsnNode) node).name) && params_isKeyDown.equals(((MethodInsnNode) node).desc))
                        {
                            if(node.getPrevious().getOpcode() == Opcodes.GETFIELD && node.getPrevious().getPrevious().getOpcode() == Opcodes.GETFIELD && node.getPrevious().getPrevious().getPrevious().getOpcode() == Opcodes.ALOAD)
                            {
                                //Pretty sure we found it at this point otherwise another code mod has changed something :/
                                foundNode = node;
                                break;
                            }
                        }
                    }
                }

                if(foundNode != null)
                {
                    AbstractInsnNode next = foundNode.getNext();
                    method.instructions.remove(foundNode.getPrevious().getPrevious().getPrevious());
                    method.instructions.remove(foundNode.getPrevious().getPrevious());
                    method.instructions.remove(foundNode.getPrevious());
                    method.instructions.remove(foundNode);
                    method.instructions.insertBefore(next, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/ControllerInput", "isRightClicking", "()Z", false));

                    Controllable.LOGGER.info("Successfully patched #processKeyBinds");
                }
                else
                {
                    Controllable.LOGGER.info("Failed to patch #processKeyBinds");
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private byte[] patchGuiContainer(byte[] bytes)
    {
        Controllable.LOGGER.info("Patching net.minecraft.client.gui.inventory.GuiContainer");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        ObfName method_mouseClicked = new ObfName("func_73864_a", "mouseClicked");
        String desc_mouseClicked = "(III)V";

        ObfName method_mouseReleased = new ObfName("func_146286_b", "mouseReleased");
        String desc_mouseReleased = "(III)V";

        for(MethodNode method : classNode.methods)
        {
            if(method_mouseReleased.equals(method.name) && method.desc.equals(desc_mouseReleased))
            {
                Controllable.LOGGER.info("Patching #mouseReleased");
                if(patchQuickMove(method))
                {
                    Controllable.LOGGER.info("Successfully patched #mouseReleased");
                }
                else
                {
                    Controllable.LOGGER.info("Failed to patch #mouseReleased");
                }

            }
            else if(method_mouseClicked.equals(method.name) && method.desc.equals(desc_mouseClicked))
            {
                Controllable.LOGGER.info("Patching #mouseClicked");
                if(patchQuickMove(method))
                {
                    Controllable.LOGGER.info("Successfully patched #mouseClicked");
                }
                else
                {
                    Controllable.LOGGER.info("Failed to patch #mouseClicked");
                }

            }
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private boolean patchQuickMove(MethodNode method)
    {
        ObfName method_isKeyDown = new ObfName("func_100015_a", "isKeyDown");
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

            if(!method_isKeyDown.equals(((MethodInsnNode) node).name))
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

            if(!method_isKeyDown.equals(((MethodInsnNode) temp).name))
            {
                continue;
            }

            foundNode = node.getPrevious();
        }

        if(foundNode != null)
        {
            AbstractInsnNode previous = foundNode.getPrevious();
            method.instructions.remove(foundNode.getNext().getNext().getNext().getNext());
            method.instructions.remove(foundNode.getNext().getNext().getNext());
            method.instructions.remove(foundNode.getNext().getNext());
            method.instructions.remove(foundNode.getNext());
            method.instructions.remove(foundNode);
            method.instructions.insert(previous, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/ControllerInput", "canQuickMove", "()Z", false));
            return true;
        }
        return false;
    }
}
