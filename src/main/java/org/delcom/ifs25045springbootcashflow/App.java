package org.delcom.ifs25045springbootcashflow;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@Meta(name = "description",
        content = "Cashflow: aplikasi pencatat keuangan pribadi dengan saldo, bukti transaksi, dan bantuan AI.")
@PWA(name = "Cashflow", shortName = "Cashflow", iconPath = "icons/icon.png")
public class App implements AppShellConfigurator {

    /**
     * Script di HTML awal (sebelum Vaadin selesai memuat) supaya halaman yang diaudit saat masih
     * loading tetap punya judul, heading level-satu, landmark main, dan indikator koneksi berlandmark.
     * Placeholder dihapus oleh LoadingIndicatorInitListener ketika UI Vaadin sudah tampil.
     */
    static final String BOOT_SCRIPT = """
            (function () {
              if (!document.title) { document.title = 'Cashflow'; }
              document.addEventListener('DOMContentLoaded', function () {
                var label = function () {
                  document.querySelectorAll('vaadin-connection-indicator').forEach(function (el) {
                    if (el.getAttribute('role') !== 'region') {
                      el.setAttribute('role', 'region');
                      el.setAttribute('aria-label', 'Status koneksi');
                    }
                  });
                };
                var boot = document.createElement('main');
                boot.id = 'boot-placeholder';
                boot.setAttribute('style', 'position:fixed;inset:0;display:flex;flex-direction:column;'
                  + 'align-items:center;justify-content:center;background:#F3F4F6;color:#1F2937;'
                  + 'font-family:sans-serif;z-index:1;');
                boot.innerHTML = '<h1 style="margin:0">Cashflow</h1><p style="margin:0.5rem 0 0">Memuat aplikasi...</p>';
                document.body.appendChild(boot);
                label();
                new MutationObserver(label).observe(document.body, { childList: true });
              });
            })();
            """;

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addInlineWithContents(Inline.Position.PREPEND, BOOT_SCRIPT, Inline.Wrapping.JAVASCRIPT);
    }

    public static void main(String[] args) throws Exception {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}