package me.petterim1.signedit;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3i;
import cn.nukkit.player.Player;
import cn.nukkit.plugin.PluginBase;

import java.util.HashMap;
import java.util.Map;

public class SignEdit extends PluginBase implements Listener {

    private Map<Player, Vector3i> editMode = new HashMap<>();

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (b instanceof BlockSignPost) {
            if (editMode.containsKey(p)) {
                editMode.put(p, b);
                showEditForm(p, b.getLevel(), b);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        editMode.remove(e.getPlayer());
    }

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent e) {
        Player p = e.getPlayer();
        if (e.getResponse() == null) return;
        if (e.getWindow().wasClosed()) return;

        if (e.getWindow() instanceof FormWindowCustom) {
            if (((FormWindowCustom) e.getWindow()).getTitle().equals("표지판 수정")) {
                String[] text = new String[4];
                text[0] = ((FormWindowCustom) e.getWindow()).getResponse().getInputResponse(1);
                text[1] = ((FormWindowCustom) e.getWindow()).getResponse().getInputResponse(2);
                text[2] = ((FormWindowCustom) e.getWindow()).getResponse().getInputResponse(3);
                text[3] = ((FormWindowCustom) e.getWindow()).getResponse().getInputResponse(4);

                Vector3i loc = editMode.get(p);

                if (loc == null) {
                    p.sendMessage("§c오류 : Location is null");
                    return;
                }

                BlockEntitySign be = (BlockEntitySign) p.getLevel().getBlockEntity(loc);

                if (be == null) {
                    p.sendMessage("§c오류! 해당 블럭의 엔티티를 찾을 수 없습니다 : " + loc2string(p.getLevel(), loc));
                } else {
                    be.setText(text);
                    p.sendMessage("§aIMPULSE>> 표지판을 수정했습니다!");
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("signedit")) {
            if (!sender.hasPermission("signedit")) {
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage("§c인게임에서만 사용 가능");
                return true;
            }

            Player p = (Player) sender;

            if (editMode.containsKey(p)) {
                editMode.remove(p);
                p.sendMessage("§e표지판 수정 모드가 §c비활성화 §e되었습니다.");
            } else {
                editMode.put(p, null);
                p.sendMessage("§e표지판 수정 모드가 §a활성화 §e되었습니다.");
            }

            return true;
        }

        return false;
    }

    private void showEditForm(Player p, Level l, Vector3i loc) {
        FormWindowCustom form = new FormWindowCustom("표지판 수정");

        BlockEntitySign be = (BlockEntitySign) l.getBlockEntity(loc);

        if (be == null) {
            form.addElement(new ElementLabel("§c오류! 해당 블럭의 엔티티를 찾을 수 없습니다 : " + loc2string(l, loc)));
        } else {
            form.addElement(new ElementLabel("§7표지판을 수정했습니다, 수정된 표지판 : " + loc2string(l, loc)));
            String[] text = be.getText();
            form.addElement(new ElementInput("", "", text[0]));
            form.addElement(new ElementInput("", "", text[1]));
            form.addElement(new ElementInput("", "", text[2]));
            form.addElement(new ElementInput("", "", text[3]));
        }

        p.showFormWindow(form);
    }

    private static String loc2string(Level l, Vector3i loc) {
        return '[' + l.getName() + "] " + loc.x + ' ' + loc.y + ' ' + loc.z;
    }
}
