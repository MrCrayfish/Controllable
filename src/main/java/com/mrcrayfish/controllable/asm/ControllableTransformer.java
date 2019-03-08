package com.mrcrayfish.controllable.asm;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

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

        ObfName methodName = new ObfName("func_147115_a", "sendClickBlockToController");
        String methodParams = "(Z)V";

        for(MethodNode method : classNode.methods)
        {
            if(methodName.equals(method.name) && method.desc.equals(methodParams))
            {
                Controllable.LOGGER.info("Patching net.minecraft.client.Minecraft#sendClickBlockToController");

                method.instructions.insert(new VarInsnNode(Opcodes.ISTORE, 1));
                method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Events", "isLeftClicking", "()Z", false));

                Controllable.LOGGER.info("Successfully patched net.minecraft.client.Minecraft#sendClickBlockToController");
                break;
            }
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
