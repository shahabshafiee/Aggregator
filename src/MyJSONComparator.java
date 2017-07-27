import java.util.Comparator;

import org.json.simple.JSONObject;


class MyJSONComparator implements Comparator<JSONObject> {


	public int compare(JSONObject o1, JSONObject o2) {
		double v1 =  (double) ((JSONObject) o1.get("prices")).get("powerCosts");
		double v3 =  (double) ((JSONObject) o2.get("prices")).get("powerCosts");
		if(v1>v3)return 1;
		else if(v1<v3)return -1;
		return 0;
	}

}
