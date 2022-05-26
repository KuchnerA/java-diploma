import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LinksSuggester {

    private List<Suggest> suggests;

    public LinksSuggester(File file) throws IOException, WrongLinksFormatException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String currentLine = reader.readLine();
        suggests = new ArrayList<>();
        while (currentLine != null) {

            String[] objs = currentLine.split("\t");
            if (objs.length != 3) {
                throw new WrongLinksFormatException("There should be 3 arguments per line: " + currentLine);
            }
            suggests.add(new Suggest(objs[0], objs[1], objs[2]));

            currentLine = reader.readLine();
        }
    }

    public List<Suggest> suggest(String text) {

        var necessary = new ArrayList<Suggest>();

        for (int i = 0; i < suggests.size(); i++) {
            var suggest = suggests.get(i);
            if (text.toLowerCase().contains(suggest.getKeyWord().toLowerCase())) {
                necessary.add(suggest);
            }
        }
        return necessary;
    }
}
