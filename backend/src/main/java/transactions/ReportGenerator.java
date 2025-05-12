package transactions;


import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator {

    String dest;
    List<Transaction> transactions;
    Map<String, String> summary;
    LinkedHashMap<String, Object> dashboards;

    public ReportGenerator(String dest, List<Transaction> transactions, Map<String, String> summary,
                           LinkedHashMap<String, Object> dashboards) {
        this.dest = dest;
        this.transactions = transactions;
        this.summary = summary;
        this.dashboards = dashboards;
    }

    public ReportGenerator(List<Transaction> transactions, Map<String, String> summary,
                           LinkedHashMap<String, Object> dashboards) {
        this.dest = "report.pdf";
        this.transactions = transactions;
        this.summary = summary;
        this.dashboards = dashboards;
    }

    public void generateFile() {
        try {
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            FontProgram fontProgram = FontProgramFactory.createFont(
                    "fonts/DejaVuSans.ttf"
            );

            PdfFont cyrillicFont = PdfFontFactory.createFont(
                    fontProgram,
                    PdfEncodings.IDENTITY_H   // embed the font in the PDF
            );

            document.setFont(cyrillicFont);

            addDashboards(document);
            addSummary(document);
            addTransactions(document);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDashboards(Document document) throws IOException {
        document.add(new Paragraph("Графики транзакций")
                .setFontSize(12)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15)
        );
        String[] periods = {"weekly", "monthly", "quarterly", "yearly"};

        for (Map.Entry<String, Object> entry : dashboards.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dashboard = (Map<String, Object>) entry.getValue();
            if (entry.getKey().equals("Dashboard 1")) {
                for (String period : periods) {
                    Image chartImage = createChart((List<List<String>>) dashboard.get(period),
                            "Количество транзакций " + "(" + period + ")");
                    document.add(chartImage);
                }
            } else if (entry.getKey().equals("Dashboard 2")) {
                for (String period : periods) {
                    Image chartImage = createMultiSeriesChart((List<List<String>>) dashboard.get(period),
                            "Количество поступлений и списаний" + "(" + period + ")",
                            "Поступления",
                            "Списания");
                    document.add(chartImage);
                }
            } else if (entry.getKey().equals("Dashboard 3")) {
                for (String period : periods) {
                    Image chartImage = createMultiSeriesChart((List<List<String>>) dashboard.get(period),
                            "Сравнение сумм поступлений и списаний" + "(" + period + ")",
                            "Поступления",
                            "Списания");
                    document.add(chartImage);
                }
            } else if (entry.getKey().equals("Dashboard 4")) {
                Image chartImage = createChartFromMap(dashboard,
                        "Статистика успешных и отмененных транзакций");
                document.add(chartImage);
            } else if (entry.getKey().equals("Dashboard 5")) {
                Image chartImage = createMultipleChartFromMap((Map<String, Integer>) dashboard.get("BankSenders"),
                        (Map<String, Integer>) dashboard.get("BankReceivers"),
                        "Суммы поступлений и списаний по банкам",
                        "Банк-отправитель",
                        "Банк-получатель");
                document.add(chartImage);
            } else if (entry.getKey().equals("Dashboard 6")) {
                Image chartImage = createMultipleChartFromMap((Map<String, Integer>) dashboard.get("Income Categories"),
                        (Map<String, Integer>) dashboard.get("Expenses Categories"),
                        "Суммы поступлений и списаний по категориям",
                        "Категории поступления",
                        "Категории списаний");
                document.add(chartImage);
            }
        }
    }

    private void setChartGraphics(JFreeChart chart) {
        chart.setBackgroundPaint(Color.white);
        chart.setAntiAlias(true);
        chart.getCategoryPlot().setBackgroundPaint(new Color(240, 240, 255));
        chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        CategoryPlot plot = chart.getCategoryPlot();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    }

    private Image createChart(List<List<String>> data, String title) throws IOException {
        List<String> labels = data.get(0);
        List<String> values = data.get(1);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < labels.size(); i++) {
            dataset.addValue(Integer.parseInt(values.get(i)), "Транзакции", labels.get(i));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title, "Период", "Число", dataset
        );

        setChartGraphics(chart);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 1000, 500);
        return new Image(ImageDataFactory.create(baos.toByteArray()));
    }

    private Image createMultiSeriesChart(List<List<String>> data, String title,
                                        String firstRowName, String secondRowName) throws IOException {
        List<String> labels = data.get(0);
        List<String> income = data.get(1);
        List<String> expense = data.get(2);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < labels.size(); i++) {
            dataset.addValue(Integer.parseInt(income.get(i)), firstRowName, labels.get(i));
            dataset.addValue(Integer.parseInt(expense.get(i)), secondRowName, labels.get(i));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title, "Период", "Сумма", dataset
        );

        setChartGraphics(chart);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 1000, 500);
        return new Image(ImageDataFactory.create(baos.toByteArray()));
    }

    private Image createChartFromMap(Map<String, Object> data, String title) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            dataset.addValue((Number) entry.getValue(), "Транзакции", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title, "Статус", "Количество", dataset
        );

        setChartGraphics(chart);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 1000, 500);
        return new Image(ImageDataFactory.create(baos.toByteArray()));
    }

    private Image createMultipleChartFromMap(Map<String, Integer> senders, Map<String, Integer> receivers,
                                            String title, String firstRowName, String secondRowName) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Integer> entry : senders.entrySet()) {
            dataset.addValue(entry.getValue(), firstRowName, entry.getKey());
        }
        for (Map.Entry<String, Integer> entry : receivers.entrySet()) {
            dataset.addValue(entry.getValue(), secondRowName, entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title, "", "Сумма", dataset
        );

        setChartGraphics(chart);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 1000, 500);
        return new Image(ImageDataFactory.create(baos.toByteArray()));
    }

    private void addSummary(Document document) {
        document.add(new AreaBreak());
        document.add(new Paragraph("Сводка")
                .setFontSize(12)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15)
        );

        List<String> headers = Arrays.asList("Количество транзакций", "Поступления", "Списания", "Разница");
        List<String> values = Arrays.asList(summary.get("total_count"),
                summary.get("total_income"),
                summary.get("total_expense"),
                summary.get("balance"));

        Table table = new Table(UnitValue.createPercentArray(headers.size()))
                .setWidth(UnitValue.createPercentValue(100));

        for (String header : headers) {
            table.addCell(new Cell().add(new Paragraph(header).setBold())
                    .setBorder(new SolidBorder(0.5F)));
        }

        for (String value : values) {
            table.addCell(new Cell().add(new Paragraph(value))
                    .setBorder(new SolidBorder(0.5F)));
        }

        Div box = new Div()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(new DeviceRgb(255, 153, 51))
                .setPadding(2)
                .setFontSize(8)
                .add(table);

        document.add(box);
    }

    private void addTransactions(Document document) {
        document.add(new Paragraph("История транзакций")
                .setFontSize(12)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15)
        );

        for (Transaction transaction : transactions) {

            List<String> headers = Arrays.asList("ID", "Тип", "Сумма", "Комментарий", "Статус",
                    "Дата", "ИНН Получателя", "Телефон Получателя", "Категория");

            List<String> values = Arrays.asList(
                    String.valueOf(transaction.getTransactionID()),
                    transaction.getType().getName(),
                    String.valueOf(transaction.getAmount()),
                    transaction.getComment(),
                    transaction.getStatus().getName(),
                    transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    String.valueOf(transaction.getRecipientTIN()),
                    transaction.getRecipientPhone(),
                    transaction.getCategory().getName()
            );

            Table table = new Table(UnitValue.createPercentArray(headers.size()))
                    .setWidth(UnitValue.createPercentValue(100));

            for (String header : headers) {
                table.addCell(new Cell().add(new Paragraph(header).setBold())
                        .setBorder(new SolidBorder(0.5F)));
            }

            for (String value : values) {
                table.addCell(new Cell().add(new Paragraph(value))
                        .setBorder(new SolidBorder(0.5F)));
            }

            Div box = new Div()
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(230, 240, 250))
                    .setPadding(2)
                    .setFontSize(8)
                    .add(table);

            document.add(box);
        }
    }
}
