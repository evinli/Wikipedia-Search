package cpen221.mp3.wikimediator;

import cpen221.mp3.fsftbuffer.Bufferable;
import org.fastily.jwiki.core.Wiki;

public class WikiPage implements Bufferable {
    public String PageName;
    public String PageText;

    public WikiPage(String stringID){
        Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
        this.PageName = stringID;
        this.PageText = wiki.getPageText(stringID);
    }

    public boolean equals(WikiPage o){
        if(this.id().equals(o.id())){
            return true;
        }
        return false;
    }
    @Override
    public String id() {
        return PageName;
    }
}
