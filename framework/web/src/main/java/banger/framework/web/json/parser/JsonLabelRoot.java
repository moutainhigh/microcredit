package banger.framework.web.json.parser;

public class JsonLabelRoot {
	String JsonString;

    public String getJsonString() {
		return JsonString;
	}

	public void setJsonString(String JsonString) {
		this.JsonString = JsonString;
	}

	public JsonLabelRoot(String JsonString)
    {
        this.JsonString = JsonString;
    }
}
