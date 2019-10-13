function initializeCoreMod() {
    return {
        'minecraft': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.Minecraft'
            },
            'transformer': function(classNode) {
                log("Patching Minecraft...");

                patch({
                    obfName: "func_147115_a",
                    name: "sendClickBlockToController",
                    desc: "(Z)V",
                    patch: patch_Minecraft_sendClickBlockToController
                }, classNode);

                patch({
                     obfName: "func_184117_aA",
                     name: "processKeyBinds",
                     desc: "()V",
                     patch: patch_Minecraft_processKeyBinds
                }, classNode);

                return classNode;
            }
        },
        'container_screen': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.gui.screen.inventory.ContainerScreen'
            },
            'transformer': function(classNode) {
                log("Patching ContainerScreen...");

                patch({
                    obfName: "func_73864_a",
                    name: "mouseClicked",
                    desc: "(DDI)Z",
                    patch: patch_ContainerScreen_mouseClicked
                }, classNode);

                patch({
                     obfName: "func_146286_b",
                     name: "mouseReleased",
                     desc: "(DDI)Z",
                     patch: patch_ContainerScreen_mouseReleased
                }, classNode);

                return classNode;
            }
        },
        'forge_ingame_gui': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraftforge.client.ForgeIngameGui'
            },
            'transformer': function(classNode) {
                log("Patching ForgeIngameGui...");

                patch({
                    obfName: "",
                    name: "renderPlayerList",
                    desc: "(II)V",
                    patch: patch_ForgeIngameGui_renderPlayerList
                }, classNode);

                return classNode;
            }
            //updateCameraAndRender(FJ)V
        },
        'screen_render_patch': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.GameRenderer'
            },
            'transformer': function(classNode) {
                log("Patching GameRenderer...");

                patch({
                    obfName: "func_195458_a",
                    name: "updateCameraAndRender",
                    desc: "(FJZ)V",
                    patch: patch_GameRenderer_updateCameraAndRender
                }, classNode);

                return classNode;
            }
        }
    };
}

function findMethod(methods, entry) {
    var length = methods.length;
    for(var i = 0; i < length; i++) {
        var method = methods[i];
        if((method.name.equals(entry.obfName) || method.name.equals(entry.name)) && method.desc.equals(entry.desc)) {
            return method;
        }
    }
    return null;
}

function patch(entry, classNode) {
    var method = findMethod(classNode.methods, entry);
    var name = classNode.name.replace("/", ".") + "#" + entry.name + entry.desc;
    if(method !== null) {
        log("Starting to patch: " + name);
        if(entry.patch(method)) {
            log("Successfully patched: " + name);
        } else {
            log("Failed to patch: " + name);
        }
    } else {
        log("Failed to find method: " + name);
    }
}

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var FrameNode = Java.type('org.objectweb.asm.tree.FrameNode');

function patch_Minecraft_sendClickBlockToController(method) {
    method.instructions.insert(new VarInsnNode(Opcodes.ISTORE, 1));
    method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/ControllerInput", "isLeftClicking", "()Z", false));
    return true;
}

function patch_Minecraft_processKeyBinds(method) {
    var findInstruction = {
        obfName: "func_151470_d",
        name: "isKeyDown",
        desc: "()Z",
        matches: function(s) {
            return s.equals(this.obfName) || s.equals(this.name);
        }
    };

    var foundNode = null;
    var instructions = method.instructions.toArray();
    var length = instructions.length;
    for (var i = 0; i < length; i++) {
        var node = instructions[i];
        if(node.getOpcode() == Opcodes.INVOKEVIRTUAL && node.getNext().getOpcode() == Opcodes.IFNE) {
            if(node instanceof MethodInsnNode && findInstruction.matches(node.name) && findInstruction.desc.equals(node.desc)) {
                if(node.getPrevious().getOpcode() == Opcodes.GETFIELD && node.getPrevious().getPrevious().getOpcode() == Opcodes.GETFIELD && node.getPrevious().getPrevious().getPrevious().getOpcode() == Opcodes.ALOAD) {
                    foundNode = node;
                    break;
                }
            }
        }
    }

    if(foundNode !== null) {
        var nextNode = foundNode.getNext();
        if(!removeNthNodes(method.instructions, foundNode, -3))
            return false;
        method.instructions.remove(foundNode);
        method.instructions.insertBefore(nextNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/ControllerInput", "isRightClicking", "()Z", false));
        return true;
    }
    return false;
}

function patch_ContainerScreen_mouseClicked(method) {
    return patchQuickMove(method);
}

function patch_ContainerScreen_mouseReleased(method) {
    return patchQuickMove(method);
}

function patchQuickMove(method) {
    var findInstruction = {
        obfName: "func_216506_a",
        name: "isKeyDown",
        desc: "(JI)Z",
        matches: function(s) {
            return s.equals(this.obfName) || s.equals(this.name);
        }
    };

    var foundNode = null;
    var instructions = method.instructions.toArray();
    var length = instructions.length;
    for (var i = 0; i < length; i++) {
        var node = instructions[i];
        if(node.getOpcode() != Opcodes.INVOKESTATIC)
            continue;
        if(node.getNext() === null || node.getNext().getOpcode() != Opcodes.IFNE)
            continue;
        if(node.getPrevious() === null || node.getPrevious().getOpcode() != Opcodes.SIPUSH)
            continue;
        if(!findInstruction.matches(node.name))
            continue;
        if(!findInstruction.desc.equals(node.desc))
            continue;
        var temp = getNthRelativeNode(node, 6);
        if(temp === null)
            continue;
        if(temp.getOpcode() != Opcodes.INVOKESTATIC && temp.getNext().getOpcode() != Opcodes.IFEQ && temp.getPrevious().getOpcode() != Opcodes.SIPUSH)
            continue;
        if(!findInstruction.matches(temp.name))
            continue;
        if(!findInstruction.desc.equals(temp.desc))
            continue;
        foundNode = node.getPrevious();
        break;
    }

    if(foundNode !== null)
    {
        var previousNode = getNthRelativeNode(foundNode, -4);
        if(!removeNthNodes(method.instructions, foundNode, 7))
            return false;
        if(!removeNthNodes(method.instructions, foundNode, -3))
            return false;
        method.instructions.remove(foundNode);
        method.instructions.insert(previousNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/ControllerInput", "canQuickMove", "()Z", false));
        return true;
    }
    return false;
}

function patch_ForgeIngameGui_renderPlayerList(method) {
    var findInstruction = {
        obfName: "func_151470_d",
        name: "isKeyDown",
        desc: "()Z",
        matches: function(s) {
            return s.equals(this.obfName) || s.equals(this.name);
        }
    };

    var foundNode = null;
    var instructions = method.instructions.toArray();
    var length = instructions.length;
    for (var i = 0; i < length; i++) {
        var node = instructions[i];
        if(node.getOpcode() != Opcodes.INVOKEVIRTUAL)
            continue;
        if(!findInstruction.matches(node.name))
            continue;
        if(getNthRelativeNode(node, -1) === null || getNthRelativeNode(node, -1).getOpcode() != Opcodes.GETFIELD)
            continue;
        if(getNthRelativeNode(node, -2) === null || getNthRelativeNode(node, -2).getOpcode() != Opcodes.GETFIELD)
            continue;
        if(getNthRelativeNode(node, -3) === null || getNthRelativeNode(node, -3).getOpcode() != Opcodes.GETFIELD)
            continue;
        if(getNthRelativeNode(node, -4) === null || getNthRelativeNode(node, -4).getOpcode() != Opcodes.ALOAD)
            continue;
        foundNode = node;
        break;
    }

    if(foundNode !== null)
    {
        if(!removeNthNodes(method.instructions, foundNode, -3))
            return false;
        method.instructions.insert(foundNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/ControllerInput", "canShowPlayerList", "()Z", false));
        method.instructions.remove(foundNode);
        return true;
    }
    return false;
}

function patch_GameRenderer_updateCameraAndRender(method) {
    var findInstruction = {
        name: "drawScreen",
        desc: "(Lnet/minecraft/client/gui/screen/Screen;IIF)V",
        matches: function(s) {
            return s.equals(this.name);
        }
    };

    var foundNode = null;
    var instructions = method.instructions.toArray();
    var length = instructions.length;
    for (var i = 0; i < length; i++) {
        var node = instructions[i];
        if(node.getOpcode() != Opcodes.INVOKESTATIC)
            continue;
        if(findInstruction.name.equals(node.name) && findInstruction.desc.equals(node.desc)) {
            foundNode = node;
            break;
        }
    }

    if(foundNode !== null) {
        var previousNode = foundNode.getPrevious();
        method.instructions.remove(foundNode);
        method.instructions.insert(previousNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/Hooks", "drawScreen", "(Lnet/minecraft/client/gui/screen/Screen;IIF)V", false))
        return true;
    }
    return false;
}

function removeNthNodes(instructions, node, n) {
    while(n > 0) {
        if(node.getNext() === null)
            return false;
        instructions.remove(node.getNext());
        n--;
    }
    while(n < 0) {
        if(node.getPrevious() === null)
            return false;
        instructions.remove(node.getPrevious());
        n++;
    }
    return true;
}

function getNthRelativeNode(node, n) {
    while(n > 0 && node !== null) {
        node = node.getNext();
        n--;
    }
    while(n < 0 && node !== null) {
        node = node.getPrevious();
        n++;
    }
    return node;
}

function log(s) {
    print("[controllable-transformer.js] " + s);
}
