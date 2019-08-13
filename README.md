![Controllable Banner](https://i.imgur.com/ILkyAfn.png)

# Controllable

I noticed a lack of support for controller for the Java Edition of Minecraft, this is where Controllable comes in. Controllable adds that ability into the game. This mod has been heavily influenced by the controls in the Bedrock Edition of the game, however it is much more configurable (coming soon) and supports more controllers (coming soon)! There is also an API available for mod developers to add controller support to your own mod.

### Features:
* On-screen button indicators (just like Bedrock Edition)
* Easy inventory management
* An easy controller mapping system to add new controllers (Coming soon)
* Many options in config to change to your liking
* A simple API with events for integration into third party mods.

### Supported Controllers:
* Sony PS4 Wireless Controller (via USB)
* XBox One Controller (via USB, Wireless Adapter, or Bluetooth Wireless)
* Other controllers coming soon!

**Note:** Support for other controllers will be a community effort. Once the controller mapping system is added, I will be accepting pull requests on GitHub for controller mappings. This is simply because I do not have access to different types of controllers.

### Developers:
If you are a developer and want to add Controllable support to your own mod, you can simply do so by adding this to your build.gradle file.

```gradle
repositories {
    maven {
        name = "CurseForge"
        url = "https://minecraft.curseforge.com/api/maven/"
    }
}

dependencies {
    compile 'controllable:Controllable:1.12.1:0.2.1'
}

minecraft {
    useDepAts = true
}
```

You will then need to run gradlew setupDecompWorkspace again as Controllable uses an access transformer. Once completed, you can start implementing controller support to your mod. The available events you can use are:

* **ControllerEvent.Move** - This event is fired when the player is moved when using a controller. This can be cancelled. An example can be found in MrCrayfish's Vehicle Mod
* **ControllerEvent.Turn** - This event is fired when the player turns it's view with a controller. This can be cancelled. 
* **ControllerEvent.ButtonInput** - This event is fired when a button is either pressed down initially and when it's released. This event can be cancelled and is useful for overriding default behavior. An example can be found in MrCrayfish's Vehicle Mod
* **AvailableActionsEvent** - This event allows you to control the button actions that are rendered to the screen. This allows you to remove or add your own actions. This event can not be cancelled. An example can be found in MrCrayfish's Vehicle Mod
* **RenderAvailableActionsEvent** - This event is fired every time the available actions are rendered. This event can be cancelled.
* **RenderPlayerPreviewEvent** - The event is fired every time the player preview in the top left corner is rendered. In case this is drawing over your GUI elements, this event can be cancelled, which stops it from renderering. An example can be found in MrCrayfish's Vehicle Mod

It's best practice that when you override any of the default controls that they should be based on a certain condition. For instance, in MrCrayfish's Vehicle Mod, controls are only overridden when riding a vehicle. It does not affect normal gameplay in any way.
