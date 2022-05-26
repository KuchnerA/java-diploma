import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        // создаём конфиг
        LinksSuggester linksSuggester = new LinksSuggester(new File("data/config"));

        // перебираем пдфки в data/pdfs
        var dir = new File("data/pdfs");
        for (var fileIn : Objects.requireNonNull(dir.listFiles())) {

            // для каждой пдфки создаём новую в data/converted
            var doc = new PdfDocument(new PdfReader(fileIn),
                    new PdfWriter(
                            new File("data/converted/"
                                    + fileIn.getName())));

            // перебираем страницы pdf
            List<String> pages = new ArrayList<>();
            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                pages.add(PdfTextExtractor.getTextFromPage(
                        doc.getPage(i)));
            }

            List<String> keyWords = new ArrayList<>();

            int i = 0;
            for (String page : pages) {

                List<Suggest> suggests = linksSuggester.suggest(String.valueOf(page));
                List<String> urls = new ArrayList<>();
                List<String> titles = new ArrayList<>();
                for (Suggest suggest :suggests) {
                    if (!keyWords.contains(suggest.getKeyWord())) {
                            urls.add(suggest.getUrl());
                            titles.add(suggest.getTitle());
                            keyWords.add(suggest.getKeyWord());
                        }
                    }



                // если в странице есть использованные ключевые слова, создаём новую страницу за ней
                i++;
                if (titles.size() > 0) {
                    var newPage = doc.addNewPage(
                            pages.indexOf(page) + i + 1);

                    var rect = new Rectangle(
                            newPage.getPageSize()).moveRight(10).moveDown(10);
                    Canvas canvas = new Canvas(newPage, rect);
                    Paragraph paragraph = new Paragraph("Suggestions:\n")
                            .setFontColor(new DeviceRgb(0, 0, 0))
                            .setBackgroundColor(new DeviceRgb(0, 255, 130));
                    paragraph.setFontSize(25);
                    // вставляем туда рекомендуемые ссылки из конфига

                    for (int j = 0; j < titles.size(); j++) {
                        PdfLinkAnnotation annotation = new PdfLinkAnnotation(rect);
                        PdfAction action = PdfAction.createURI(urls.get(j));
                        annotation.setAction(action);
                        Link link = new Link(titles.get(j), annotation);
                        paragraph.add(link.setUnderline());
                        paragraph.add("\n");
                    }

                    canvas.add(paragraph);
                    canvas.close();
                }
            }
            doc.close();
        }
    }
}
