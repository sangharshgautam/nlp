package me.foldl.corenlp_summarizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.co.sangharsh.nlp.service.PosUtil;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.ReaderInputStream;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.XMLUtils;

public class DocumentFrequencyCounter {

  private static final MaxentTagger tagger =
      new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english/english-left3words-distsim.tagger");
  
 /* private static final MaxentTagger tagger =
	      new MaxentTagger("edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");*/

  private static final int MAX_SENTENCE_LENGTH = 100;

  private static final Pattern headingSeparator = Pattern.compile("[-=]{3,}");
  private static final Pattern paragraphMarker =
      Pattern.compile("</?(?:TEXT|P)>(\n|$)");

//  private static final SpanishTreebankLanguagePack tlp = new SpanishTreebankLanguagePack();
//  private static final TokenizerFactory<? extends HasWord> tokenizerFactory = tlp.getTokenizerFactory();

  /**
   * Get an IDF map for the given document string.
   *
   * @param document
   * @return
   */
  private static Counter<String> getIDFMapForDocument(String document) {
    // Clean up -- remove some Gigaword patterns that slow things down
    // / don't help anything
    document = headingSeparator.matcher(document).replaceAll("");

    DocumentPreprocessor preprocessor = new DocumentPreprocessor(new StringReader(document));
//    preprocessor.setTokenizerFactory(tokenizerFactory);

    Counter<String> idfMap = new ClassicCounter<String>();
    for (List<HasWord> sentence : preprocessor) {
      if (sentence.size() > MAX_SENTENCE_LENGTH)
        continue;

      List<TaggedWord> tagged = tagger.tagSentence(sentence);

      for (TaggedWord w : tagged) {
        String tag = w.tag();
		if (PosUtil.isNoun(tag))
          idfMap.incrementCount(w.word());
      }
    }

    return idfMap;
  }


  public static final String TAG_DOCUMENT = "DOC";
  public static final String TAG_TEXT = "TEXT";

  private static String getFullTextContent(Element e) throws TransformerException {
    TransformerFactory transFactory = TransformerFactory.newInstance();
    Transformer transformer = transFactory.newTransformer();
    StringWriter buffer = new StringWriter();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(new DOMSource(e),
                          new StreamResult(buffer));
    String str = buffer.toString();

    // Remove paragraph markers
    str = paragraphMarker.matcher(str).replaceAll("");

    return str;
  }

  /**
   * Get an IDF map for all the documents in the given file.
   * @param file
   * @return
 * @throws ClassNotFoundException 
   */
  private static Counter<String> getIDFMapForFile(String name, Reader file)
    throws SAXException, IOException, TransformerException, ClassNotFoundException {

    DocumentBuilder parser = XMLUtils.getXmlParser();
    Document xml = parser.parse(new ReaderInputStream(file));
    NodeList docNodes = xml.getDocumentElement().getElementsByTagName(TAG_DOCUMENT);

    Element doc;
    Counter<String> idfMap = new ClassicCounter<String>();
    int length = docNodes.getLength();
    CounterWrapper counterHolder = read(name);
	
    for (int i = 0; i < 4500; i++) {
			doc = (Element) docNodes.item(i);
			String id = doc.getAttribute("id");
			Counter<String> idfMapForDocument;
			if(counterHolder.counterMap().containsKey(id)){
				idfMapForDocument = counterHolder.counterMap().get(id);
			}else{
				NodeList texts = doc.getElementsByTagName(TAG_TEXT);
				assert texts.getLength() == 1;

				Element text = (Element) texts.item(0);
				String textContent = getFullTextContent(text);

				idfMapForDocument = getIDFMapForDocument(textContent);
				System.out.println((i+1)+" of "+length+" "+idfMapForDocument);
				counterHolder.counterMap().put(id, idfMapForDocument);
				save(counterHolder);
			}
			
			idfMap.addAll(idfMapForDocument);
			// Increment magic counter
			idfMap.incrementCount("__all__");
    }

    return idfMap;
  }
  
  private static CounterWrapper read(String name) throws ClassNotFoundException, IOException {
	  CounterWrapper counterWrapper = new CounterWrapper(name);
	  File file = new File(counterWrapper.fileName());
	  if(!file.exists()){
		save(counterWrapper);
	  }
	  ObjectInputStream ois = new ObjectInputStream(new FileInputStream(counterWrapper.fileName()));
	  try{
		  counterWrapper =  (CounterWrapper)ois.readObject();
	  }catch(java.io.EOFException e){
		  FileUtils.copyFile(new File(counterWrapper.fileName()+".bk"), file);
		  return read(name);
	  }finally{
		  if(ois!=null){
			  ois.close();
		  }
	  }
	  return counterWrapper;
}

private static void save(CounterWrapper counterWrapper) throws FileNotFoundException, IOException {
	File file = new File(counterWrapper.fileName());
	if(file.exists()){
		FileUtils.copyFile(file, new File(counterWrapper.fileName()+".bk"), true);
	}
	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
  	oos.writeObject(counterWrapper);
  	oos.close();
  }

private static final class FileIDFBuilder implements Callable<Counter<String>> {
    private final File file;

    public FileIDFBuilder(File file) {
      this.file = file;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Counter<String> call() throws Exception {
      // We need to hallucinate some overarching document tag.. because the Gigaword files don't
      // have them :/
      String fileContents = IOUtils.slurpFile(file);
      fileContents = "<docs>" + fileContents + "</docs>";

      return getIDFMapForFile(file.getName(), new StringReader(fileContents));
    }
  }

  private static final String OUT_FILE = "df-counts.ser";
  private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  public static void main(String[] args) throws InterruptedException, ExecutionException,
    IOException {
    ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<Counter<String>>> futures = new ArrayList<Future<Counter<String>>>();

    for (String filePath : args)
      futures.add(pool.submit(new FileIDFBuilder(new File(filePath))));

    int finished = 0;
    Counter<String> overall = new ClassicCounter<String>();

    for (Future<Counter<String>> future : futures) {
      System.err.printf("%s: Polling future #%d / %d%n",
          dateFormat.format(new Date()), finished + 1, args.length);
      Counter<String> result = future.get();
      finished++;
      System.err.printf("%s: Finished future #%d / %d%n",
          dateFormat.format(new Date()), finished, args.length);

      System.err.printf("\tMerging counter.. ");
      overall.addAll(result);
      System.err.printf("done.%n");
    }
    pool.shutdown();

    System.err.printf("\n%s: Saving to '%s'.. ", dateFormat.format(new Date()),
        OUT_FILE);
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(OUT_FILE));
    oos.writeObject(overall);
    oos.close();
    System.err.printf("done.%n");
  }

}