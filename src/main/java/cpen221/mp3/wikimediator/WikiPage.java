package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.Bufferable;
import org.fastily.jwiki.core.Wiki;

public class WikiPage implements Bufferable {
    public String PageName;
    public String PageText;

    /**
     * Abstraction function:
     *      AF(PageName) = String ID of the page
     *      AF(PageText) = text of the wikipedia page
     */

    /**
     * Rep invariant:
     *      PageName and PageText != null
     */

    /**
     * Creates a WikiPage given a page name
     *
     * @param stringID page name of a wikipedia page
     */
    public WikiPage(String stringID){
        Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
        this.PageName = stringID;
        this.PageText = wiki.getPageText(stringID);
    }

    @Override
    public boolean equals(Object o){
        WikiPage page = (WikiPage) o;
        if(this.id().equals(page.id())){
            return true;
        }
        return false;
    }

    public String id() {
        return PageName;
    }

//    @Override
//    public int hashCode() {
//        return ;
//    }
}
