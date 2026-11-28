package org.delcom.ifs25045springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi indikator loading global untuk tindakan yang memerlukan waktu tunggu.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    static final String LANDMARK_SCRIPT = """
            const boot = document.getElementById('boot-placeholder');
            if (boot) {
              boot.remove();
            }
            const markIndicator = () => {
              document.querySelectorAll('vaadin-connection-indicator').forEach(el => {
                if (el.getAttribute('role') !== 'region') {
                  el.setAttribute('role', 'region');
                  el.setAttribute('aria-label', 'Status koneksi');
                }
              });
            };
            markIndicator();
            new MutationObserver(markIndicator).observe(document.body, { childList: true });
            """;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration conf = uiEvent.getUI().getLoadingIndicatorConfiguration();
            conf.setApplyDefaultTheme(true);
            conf.setFirstDelay(150);
            conf.setSecondDelay(600);
            conf.setThirdDelay(1500);
            uiEvent.getUI().getPage().executeJs("document.documentElement.lang = 'id'");
            // Hapus placeholder awal (App.BOOT_SCRIPT) dan pastikan indikator koneksi berada di landmark.
            uiEvent.getUI().getPage().executeJs(LANDMARK_SCRIPT);
        });
    }
}