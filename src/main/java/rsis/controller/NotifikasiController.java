package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rsis.model.Notifikasi;
import rsis.model.User;
import rsis.service.NotifikasiService;

import java.util.List;

@Controller
@RequestMapping("/notifikasi")
public class NotifikasiController {

    @Autowired
    private NotifikasiService notifikasiService;

    @GetMapping
    public String listNotifikasi(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "notifikasi");

        List<Notifikasi> notifikasis = notifikasiService.getNotifikasiByPenerimaId(user.getIdUser());
        model.addAttribute("notifikasi", notifikasis);

        return "notifikasi/list";
    }

    @PostMapping("/mark-as-read/{id}")
    @ResponseBody
    public String markAsRead(@PathVariable String id) {
        try {
            notifikasiService.markAsRead(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/mark-all-as-read")
    @ResponseBody
    public String markAllAsRead(Model model) {
        try {
            User user = (User) model.getAttribute("currentUser");
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            List<Notifikasi> notifikasis = notifikasiService.getNotifikasiByPenerimaId(user.getIdUser());
            for (Notifikasi notif : notifikasis) {
                notifikasiService.markAsRead(notif.getIdNotifikasi());
            }
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public String deleteNotifikasi(@PathVariable String id) {
        try {
            notifikasiService.deleteNotifikasi(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/count")
    @ResponseBody
    public Long getUnreadCount(Model model) {
        try {
            User user = (User) model.getAttribute("currentUser");
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            return notifikasiService.getUnreadCount(user.getIdUser());
        } catch (Exception e) {
            return 0L;
        }
    }
}
