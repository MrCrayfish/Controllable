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

        boolean isObfuscated = !name.equals(transformedName);
        if(transformedName.equals("net.minecraft.client.Minecraft"))
        {
            return patchMinecraft(bytes, isObfuscated);
        }
        return bytes;
    }

    private byte[] patchMinecraft(byte[] bytes, boolean isObfuscated)
    {
        Controllable.LOGGER.info("Applying ASM...");

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
                Controllable.LOGGER.info("Patching net.minecraft.client.Minecraft#sendClickBlockToController");

                method.instructions.insert(new VarInsnNode(Opcodes.ISTORE, 1));
                method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Events", "isLeftClicking", "()Z", false));

                Controllable.LOGGER.info("Successfully patched net.minecraft.client.Minecraft#sendClickBlockToController");
            }
            else if(method_processKeyBinds.equals(method.name) && method.desc.equals(params_processKeyBinds))
            {
                Controllable.LOGGER.info("Patching net.minecraft.client.Minecraft#processKeyBinds");

                ObfName method_onStoppedUsingItem = new ObfName("func_78766_c", "onStoppedUsingItem");
                ObfName method_isKeyDown = new ObfName("func_100015_a", "isKeyDown");

                InsnNode target;
                for(AbstractInsnNode node : method.instructions.toArray())
                {
                    if(node instanceof MethodInsnNode && method_onStoppedUsingItem.equals(((MethodInsnNode) node).name))
                    {
                        if(node.getPrevious().getOpcode() == Opcodes.GETFIELD)
                        {
                            AbstractInsnNode foundNode = null;
                            AbstractInsnNode currentNode = node;
                            while(true)
                            {
                                currentNode = currentNode.getPrevious();

                                if(currentNode == null)
                                    break;

                                if(currentNode.getOpcode() == Opcodes.INVOKEVIRTUAL && currentNode.getNext().getOpcode() == Opcodes.IFNE)
                                {
                                    if(currentNode instanceof MethodInsnNode && method_isKeyDown.equals(((MethodInsnNode) currentNode).name))
                                    {
                                        Controllable.LOGGER.info("It has the correct method");
                                        if(currentNode.getPrevious().getOpcode() == Opcodes.GETFIELD && currentNode.getPrevious().getPrevious().getOpcode() == Opcodes.GETFIELD)
                                        {
                                            Controllable.LOGGER.info("Previous fields check out");
                                            //Pretty sure we found it at this point otherwise another code mod has changed something :/
                                            foundNode = currentNode;
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
                                method.instructions.insertBefore(next, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Events", "isRightClicking", "()Z", false));

                                Controllable.LOGGER.info("Successfully patched net.minecraft.client.Minecraft#processKeyBinds");
                            }
                            else
                            {
                                Controllable.LOGGER.info("Failed to patch net.minecraft.client.Minecraft#processKeyBinds");
                            }
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
