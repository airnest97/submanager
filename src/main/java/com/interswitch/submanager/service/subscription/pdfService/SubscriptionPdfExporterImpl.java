package com.interswitch.submanager.service.subscription.pdfService;

import com.interswitch.submanager.dtos.SubscriptionDto;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SubscriptionPdfExporterImpl implements SubscriptionPdfExporter{

    private final List<SubscriptionDto> subscriptionResponseList;

    public SubscriptionPdfExporterImpl(List<SubscriptionDto> subscriptionResponseList) {
        this.subscriptionResponseList = subscriptionResponseList;
    }


    @Override
    public void export(HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A2);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);
        font.setColor(Color.BLUE);

        Paragraph p = new Paragraph("List of Subscriptions", font);
        p.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.5f, 3.0f, 3.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f });
        table.setSpacingBefore(10);

        writeTableHeader(table);
        writeTableData(table);

        document.add(table);

        document.close();

    }

    private String formatDate(LocalDate localDate){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dateTimeFormatter.format(localDate);
    }

    private void writeTableData(PdfPTable table) {
        for (SubscriptionDto subscriptionResponse : subscriptionResponseList) {
            table.addCell(String.valueOf(subscriptionResponse.getId()));
            table.addCell(subscriptionResponse.getNameOfSubscription());
            table.addCell(subscriptionResponse.getDescription());
            table.addCell(String.valueOf(subscriptionResponse.getPriceOfSubscription()));
            table.addCell(String.valueOf(subscriptionResponse.getCategory()));
            table.addCell(String.valueOf(subscriptionResponse.getRecurringPayment()));
            table.addCell(formatDate(subscriptionResponse.getNextPayment()));
            table.addCell(String.valueOf(subscriptionResponse.getPaymentCycle()));
            table.addCell(formatDate(subscriptionResponse.getDateAdded()));
        }

    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.BLUE);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        cell.setPhrase(new Phrase("Sub ID", font));

        table.addCell(cell);

        cell.setPhrase(new Phrase("Name", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Description", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Amount", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Category", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Recurring", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Next Payment", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Cycle", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Date Added", font));
        table.addCell(cell);
    }
}
