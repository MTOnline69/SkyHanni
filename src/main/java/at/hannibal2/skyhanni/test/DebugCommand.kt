package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.StringUtils.equalsIgnoreColor

object DebugCommand {

    fun command(args: Array<String>) {
        if (args.size == 2 && args[0] == "profileName") {
            HypixelData.profileName = args[1].lowercase()
            LorenzUtils.chat("§eManually set profileName to '${HypixelData.profileName}'")
            return
        }
        val list = mutableListOf<String>()
        list.add("```")
        list.add("= Debug Information for SkyHanni ${SkyHanniMod.version} =")
        list.add("")

        val search = args.getOrNull(0)
        list.add(
            if (search != null) {
                if (search.equalsIgnoreColor("all")) {
                    "search for everything."
                } else "search: '$search'"
            } else "search not specified, showing only interesting stuff"
        )

        val event = DebugDataCollectEvent(list, search)

        event.title("Player")
        event.ignore {
            add("name: '${LorenzUtils.getPlayerName()}'")
            add("uuid: '${LorenzUtils.getPlayerUuid()}'")
        }

        event.title("Repo Auto Update")
        if (SkyHanniMod.feature.dev.repoAutoUpdate) {
            event.ignore("normal enabled")
        } else {
            event.addData("The repo does not auto update because auto update is disabled!")
        }

        event.title("Global Render")
        if (SkyHanniDebugsAndTests.globalRender) {
            event.ignore("normal enabled")
        } else {
            event.addData {
                add("Global renderer is disabled!")
                add("No renderable elements from SkyHanni will show up anywhere!")
            }
        }

        event.title("SkyBlock Status")
        if (!LorenzUtils.onHypixel) {
            event.addData("not on Hypixel")
        } else {
            if (!LorenzUtils.inSkyBlock) {
                event.addData("not on SkyBlock, but on Hypixel")
            } else {
                if (LorenzUtils.skyBlockIsland == IslandType.UNKNOWN) {
                    event.addData("Unknown SkyBlock island!")
                } else {
                    event.ignore {
                        add("on Hypixel SkyBlock")
                        add("skyBlockIsland: ${LorenzUtils.skyBlockIsland}")
                        add("skyBlockArea: '${LorenzUtils.skyBlockArea}'")
                    }
                }
            }
        }

        event.title("Profile Name")
        if (!LorenzUtils.inSkyBlock) {
            event.ignore("Not on SkyBlcok")
        } else {
            if (HypixelData.profileName != "") {
                event.ignore("profileName: '${HypixelData.profileName}'")
            } else {
                event.addData("profile name is empty!")
            }
        }


        event.title("Profile Type")
        if (!LorenzUtils.inSkyBlock) {
            event.ignore("Not on SkyBlcok")
        } else {
            val classic = !LorenzUtils.noTradeMode
            if (classic) {
                event.ignore("on classic")
            } else {
                if (HypixelData.ironman) {
                    event.addData("on ironman")
                }
                if (HypixelData.stranded) {
                    event.addData("on stranded")
                }
                if (HypixelData.bingo) {
                    event.addData("on bingo")
                }
            }
        }

        event.postAndCatch()

        if (event.empty) {
            list.add("")
            list.add("Nothing interesting to show right now!")
            list.add("Looking for something specific? /shdebug <search>")
            list.add("Wanna see everything? /shdebug all")
        }

        list.add("```")
        OSUtils.copyToClipboard(list.joinToString("\n"))
        LorenzUtils.chat("§eCopied SkyHanni debug data in the clipboard.")
    }
}