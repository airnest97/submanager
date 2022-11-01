package com.interswitch.submanager.service.subscription.pdfService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface SubscriptionPdfExporter {
    void export(HttpServletResponse httpServletResponse) throws IOException;
}
