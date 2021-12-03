package cpen221.mp3.fsftbuffer;

import cpen221.mp3.wikimediator.WikiPage;
import org.fastily.jwiki.core.Wiki;

public class Buffer implements Bufferable{
    public String PageName;
    public String PageText;

    public Buffer(String stringID){
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
