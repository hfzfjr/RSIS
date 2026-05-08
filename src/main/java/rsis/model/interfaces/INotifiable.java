package rsis.model.interfaces;

import rsis.model.Notifikasi;

public interface INotifiable {
    void terimaNotifikasi(Notifikasi notif);

    String getEmail();
}
