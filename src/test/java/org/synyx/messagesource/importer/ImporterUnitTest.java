/**
 * 
 */
package org.synyx.messagesource.importer;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.synyx.messagesource.MessageAcceptor;
import org.synyx.messagesource.MessageProvider;
import org.synyx.messagesource.Messages;


/**
 * @author Marc Kannegiesser - kannegiesser@synyx.de
 */
public class ImporterUnitTest {

    private SimpleMessageAcceptor source;
    private SimpleMessageAcceptor target;
    private Messages messages;


    @Before
    public void setup() {

        messages = new Messages();
        messages.addMessage(Locale.GERMAN, "key", "value");

        source = new SimpleMessageAcceptor();
        source.setMessages("foo", messages);

        target = new SimpleMessageAcceptor();
    }


    @Test
    public void testImport() {

        Importer importer = new Importer(source, target);
        importer.importMessages("foo");

        Messages fooMessages = target.getMessages("foo");

        assertThat(fooMessages.getMessage(Locale.GERMAN, "key"), is("value"));
        assertThat(fooMessages.getLocales().size(), is(1));
    }


    @Test
    public void testImportTwice() {

        Importer importer = new Importer(source, target);
        // import the old one with only German: key -> value
        importer.importMessages("foo");

        // now remove the German: key -> value and add another German: bar -> value
        messages.addMessage(Locale.GERMAN, "bar", "barvalue");
        messages.removeMessage(Locale.GERMAN, "key");

        // import once again... all old messages should be gone
        importer.importMessages("foo");
        Messages fooMessages = target.getMessages("foo");

        assertThat(fooMessages.getMessage(Locale.GERMAN, "bar"), is("barvalue"));
        assertThat(fooMessages.getMessage(Locale.GERMAN, "key"), is((String) null));
        assertThat(fooMessages.getLocales().size(), is(1));

    }


    @Test
    public void testFileSystemImport() throws IOException {

        File file = null;
        File tempfile = null;
        try {
            String basename = "base";

            Properties p = new Properties();
            p.setProperty("key", "value");

            tempfile = Files.createTempDirectory("messagesourcetext").toFile();
            if (tempfile.exists()) {
            }

            file = new File(tempfile, basename + ".properties");
            PrintStream stream = new PrintStream(file);
            p.store(stream, "generated by " + this.getClass().getName());
            stream.close();

            Importer importer = new Importer(tempfile, target);
            importer.importMessages(basename);

            Messages messages = target.getMessages(basename);

            String imported = messages.getMessage(null, "key");

            assertThat(imported, is("value"));

        } finally {
            if (file != null) {
                file.delete();

            }

            if (tempfile != null) {
                tempfile.delete();
            }
        }

    }

    class SimpleMessageAcceptor implements MessageAcceptor, MessageProvider {

        Map<String, Messages> messageMap = new HashMap<String, Messages>();


        /*
         * (non-Javadoc)
         * 
         * @see org.synyx.messagesource.MessageAcceptor#setMessages(java.lang.String, org.synyx.messagesource.Messages)
         */
        public void setMessages(String basename, Messages messages) {

            messageMap.put(basename, messages);

        }


        /*
         * (non-Javadoc)
         * 
         * @see org.synyx.messagesource.MessageProvider#getMessages(java.lang.String)
         */
        public Messages getMessages(String basename) {

            return messageMap.get(basename);
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.synyx.messagesource.MessageProvider#getAvailableBaseNames()
         */
        public List<String> getAvailableBaseNames() {

            return new ArrayList<String>(messageMap.keySet());
        }

    }

}
