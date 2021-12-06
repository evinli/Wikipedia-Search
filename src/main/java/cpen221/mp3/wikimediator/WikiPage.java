package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.Bufferable;
import org.fastily.jwiki.core.Wiki;

/**
 * An immutable data type that represents a wikipedia page.
 */
public class WikiPage implements Bufferable {
    private String pageName;
    private String pageText;

    /**
     * Abstraction function:
     *      AF(pageName) = wikipedia page name
     *      AF(pageText) = text contained on the wikipedia page
     */

    /**
     * Rep invariant:
     *      pageName and pageText != null
     */

    /**
     * Creates a WikiPage object given a page name.
     *
     * @param stringID name of the wikipedia page
     */
    public WikiPage(String stringID){
        Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
        this.pageName = stringID;
        this.pageText = wiki.getPageText(stringID);
    }

    /**
     * Returns the page name associated with the WikiPage object.
     *
     * @return a String representing the wikipedia page name
     */
    public String id() {
        return pageName;
    }

    /**
     * Returns the page text associated with the WikiPage object.
     *
     * @return a String representing the text contained on a wikipedia page
     */
    public String text() {
        return pageText;
    }

    @Override
    public boolean equals(Object o){
        WikiPage page = (WikiPage) o;
        if(this.id().equals(page.id())){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return pageName.hashCode()^ pageText.hashCode();
    }
}
