![Controllable Banner](https://i.imgur.com/ILkyAfn.png)

[![Download](https://img.shields.io/static/v1?label=&message=Download&color=2d2d2d&labelColor=dddddd&style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAALGPC/xhBQAAAAlwSFlzAAALEQAACxEBf2RfkQAAAAd0SU1FB98BHA41LJJkRpIAAAAYdEVYdFNvZnR3YXJlAHBhaW50Lm5ldCA0LjAuMvvhp8YAAAGGSURBVDhPjZK9SgNBFIVHBEUE8QeMhLAzdzdYBFL7CmnstEjjO4iFjWClFiGJm42VFpYSUptKRfABFBQE7UUI2AmGmPXM5k6cjFE8cJjZ3fPduTOzIiJarvl+bPmuLuWS+K/KmcwUoK4pEGoTnetvYTY7GUpZOCTa0sZi+QRyhQ83pkBShKjDUBvzGKNxr6rU8W4uN8FoX4CKEcB6EMR61OZOEtApEqNIndFvAbiNuAB30QeU+sDYBXSCdw3MrzHvlnx/kdFkC3kU6NkwF3iM0ukFnNM8R0UplZquKHWE85nhV0Kg4o4GrDMwre5x5G9h9aaBHL/VPI849rtqRFfwqALaIcd+F87gYnDy1ha0sY12VcoiR0cLq5/ae3e3gyLbHB2tMAgKQ1fXh+z5a1mpDXQyy8hPIdgyAEMDW8+fWOQJHbfgzXUhxhkXYh/3jcC9C7s2V43c+9DPpHXgeXMIXbqQbV7gAV5hbFixEGMVolUEmvjjXuBeRcoOnp/R/hm81hi0LsQX8OcRBvBjZ8YAAAAASUVORK5CYII=)](https://mrcrayfish.com/mods?id=controllable) ![Minecraft](https://img.shields.io/static/v1?label=&message=1.16%20|%201.15%20|%201.14%20|%201.12&color=2d2d2d&labelColor=dddddd&style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAZdEVYdFNvZnR3YXJlAHBhaW50Lm5ldCA0LjAuMjCGJ1kDAAACoElEQVQ4T22SeU8aURTF/ULGtNRWWVQY9lXABWldIDPIMgVbNgEVtaa0damiqGBdipXaJcY2ofEf4ycbTt97pVAabzK5b27u+Z377kwXgK77QthRy7OfXbeJM+ttqKSXN8sdwbT/A0L7elmsYqrPHZmROLPh5YkV4oEBwaKuHj+yyJptLDoAhbq3O1V1XCVObY3FL24mfn5oRPrcwSCRfQOyNWcjVjZdCbtcdwcgXrXUspdOKbDN/XE9tiBJMhXHT60gUIT2dMhcDLMc3NVKQklz0QIkf5qlyEcO6Qs7yPhMJB4amDMFimQSmqNlE8SKAZFzDfxHfVILIIZ10sJ3OwIbcqSuiOjchkzNCboHev9o2YhgiUP8mxnLN24I6/3ghYdtQG5iUMpFBuCP9iKwLsfiLyeCp2rMnZgwX3NArGoxW1Ridl+BzLEVKa8KSxOqNmDdz0kFnxaLHhWEgAyZigWhHXL+pEDy2ozsDxv8vAzTnh7w5kcghqCaFmCT10of4iPIT2mRdPUh4HoCcVwBH/8Ac2kzUkEV5r3EfVSOvbAJa5NDyI0r2oDtWb1EClh+OoC3Pg7v/Bw7p939yI4rsRW2Y3lKh01eh7WpIRyKZqzyjjYgPdIvlaMWRqYuG7wWryYHsRM0sFolZiPvQ3jheIwSmSBPdkByG/B6Wi3RYiVmRX7GiAPiUCRisii8D+jZNKvPBrHCW1GY0bAz6WkDCtOaSyKQFsi4K5NqNiZtehN2Y5uAShETqolhBqJXpfdPuPsuWwAaRdHSkxdc11mPqkGnyY4pyKbpl1GyJ0Pel7yqBoFcF3zqno5f+d8ohYy9Sx7lzQpxo1eirluCDgt++00p6uxttrG4F/A39sJGZWZMfrcp6O6+5kaVzXJHAOj6DeSs8qw5o8oxAAAAAElFTkSuQmCC) ![Curseforge](http://cf.way2muchnoise.eu/full_controllable_downloads.svg?badge_style=for_the_badge)

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
