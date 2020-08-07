function initializeCoreMod() {
    return {
        'send_click': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.Minecraft',
                'methodName': 'func_147115_a',
                'methodDesc': '(Z)V'
            },
            'transformer': function(method) {
                wrapInvoke(patch_Minecraft_sendClickBlockToController, method, "Minecraft#func_147115_a");
                return method;
            }
        },
        'process_keys': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.Minecraft',
                'methodName': 'func_184117_aA',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                wrapInvoke(patch_Minecraft_processKeyBinds, method, "Minecraft#func_184117_aA");
                return method;
            }
        },
        'mouse_clicked': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.gui.screen.inventory.ContainerScreen',
                'methodName': 'func_73864_a',
                'methodDesc': '(DDI)Z'
            },
            'transformer': function(method) {
                wrapInvoke(patch_ContainerScreen_mouseClicked, method, "ContainerScreen#func_73864_a");
                return method;
            }
        },
        'mouse_released': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.gui.screen.inventory.ContainerScreen',
                'methodName': 'func_146286_b',
                'methodDesc': '(DDI)Z'
            },
            'transformer': function(method) {
                wrapInvoke(patch_ContainerScreen_mouseReleased, method, "ContainerScreen#func_146286_b");
                return method;
            }
        },
        'render_player_list': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.client.ForgeIngameGui',
                'methodName': 'renderPlayerList',
                'methodDesc': '(IILcom/mojang/blaze3d/matrix/MatrixStack;)V'
            },
            'transformer': function(method) {
                wrapInvoke(patch_ForgeIngameGui_renderPlayerList, method, "ForgeIngameGui#renderPlayerList");
                return method;
            }
        },
        'render_selected_item': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.client.ForgeIngameGui',
                'methodName': 'renderRecordOverlay',
                'methodDesc': '(IIFLcom/mojang/blaze3d/matrix/MatrixStack;)V'
            },
            'transformer': function(method) {
                wrapInvoke(patch_IngameGui_renderSelectedItem, method, "ForgeIngameGui#renderRecordOverlay");
                return method;
            }
        },
        'screen_render_patch': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.GameRenderer',
                'methodName': 'func_195458_a',
                'methodDesc': '(FJZ)V'
            },
            'transformer': function(method) {
                wrapInvoke(patch_GameRenderer_updateCameraAndRender, method, "GameRenderer#func_195458_a");
                return method;
            }
        },
        'selected_item_name': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.gui.IngameGui',
                'methodName': 'func_238453_b_',
                'methodDesc': '(Lcom/mojang/blaze3d/matrix/MatrixStack;)V'
            },
            'transformer': function(method) {
                wrapInvoke(patch_IngameGui_renderSelectedItem, method, "IngameGui#func_194801_c");
                return method;
            }
        }
    };
}

function wrapInvoke(patcher, method, name) {
    log("Patching " + name);
    if(patcher(method)) {
        log("Successfully patched " + name);
    } else {
        log("Failed to patch " + name);
    }
}

var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
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
    method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "isLeftClicking", "()Z", false));
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
        method.instructions.insertBefore(nextNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "isRightClicking", "()Z", false));
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
        method.instructions.insert(previousNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "canQuickMove", "()Z", false));
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

    log("Instructions " + method.instructions.toArray() + " length " + " " + length)
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
        method.instructions.insert(foundNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "canShowPlayerList", "()Z", false));
        method.instructions.remove(foundNode);
        return true;
    }
    return false;
}

function patch_GameRenderer_updateCameraAndRender(method) {
    var findInstruction = {
        name: "render",
        desc: "(Lnet/minecraft/client/gui/screen/Screen;Lcom/mojang/blaze3d/matrix/MatrixStack;IIF)V",
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
        method.instructions.insert(previousNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "drawScreen", "(Lnet/minecraft/client/gui/screen/Screen;Lcom/mojang/blaze3d/matrix/MatrixStack;IIF)V", false));
        return true;
    }
    return false;
}

function patch_IngameGui_renderSelectedItem(method) {
    var findInstruction = {
        name: "pushMatrix",
        desc: "()V",
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
        method.instructions.insert(foundNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mrcrayfish/controllable/client/Hooks", "applyHotbarOffset", "()V", false));
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
