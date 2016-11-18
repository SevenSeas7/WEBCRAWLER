import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebCrawler {
	ArrayList<String> allurls = new ArrayList<String>(); 
	ArrayList<String> notCrawlurls = new ArrayList<String>(); 
	//Thread safe queries. 
	final static Vector <String> sqlqueries = new Vector<String>() ;
	int threadCount = 5;
	int count = 0; 
	public static final Object signal = new Object();
	
	public static void main(String[] args) {
		final WebCrawler wc = new WebCrawler();
		String[] tagtypes= { "互联网","编程","算法"};
		int numinpage = 20;
		int pagenumber= 5; 
		wc.addUrl("https://book.douban.com/tag/",tagtypes,numinpage,pagenumber);
		long start= System.currentTimeMillis();
		
		System.out.println("开始爬虫。。。。。");
		wc.begin();
		
		while(true){
			if(wc.notCrawlurls.isEmpty()&& Thread.activeCount() == 1||wc.count==wc.threadCount){
				long end = System.currentTimeMillis();
				System.out.println("爬虫总共耗时"+(end-start)/1000+"秒");
				SQL.Connect();
				SQL.InsertBookInfo(sqlqueries);
				SQL.Close();
				System.exit(1);
			}
			
		}
	}
	private void begin() {
		for(int i=0;i<threadCount;i++){
			new Thread(new Runnable(){
				public void run() {
					while (true) { 
						String tmp = getUrl();
						if(tmp!=null){
							SQL.Connect();
							crawler(tmp);
						}else{
							synchronized(signal){ 
								try {
									count++;
									signal.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			},"thread-"+i).start();
		}
	}
	public synchronized String getUrl() {
		if(notCrawlurls.isEmpty()){
			return null;
		}
		String tmpUrl;
		tmpUrl= notCrawlurls.get(0);
		notCrawlurls.remove(0);
		return tmpUrl;
	}
	
	public synchronized void addUrl(String url, String[] types, int numinpage, int pagenumber){
			for (String str:types){
				for(int i=0; i<pagenumber; i++){
					notCrawlurls.add(url+str+"?start="+numinpage*i+"&type=S");
					allurls.add(url+str+"?start="+numinpage*i+"&type=S");
				}
			}
	}
	
	public void crawler(String sUrl){
		URL url;
		try {
				url = new URL(sUrl);
				URLConnection urlconnection = url.openConnection();
				urlconnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
				BufferedReader bReader = new BufferedReader(new InputStreamReader(url.openStream(),"utf-8"));
				StringBuffer sb = new StringBuffer();
				String rLine = null;
				while((rLine=bReader.readLine())!=null){
					sb.append(rLine);
					sb.append("/r/n");
				}
				parseContext(sUrl, sb.toString());
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void parseContext(String url, String context) {
		String[] regex = {"/\" title=\"(.*?)\"",
				"<div class=\"pub\">(.*?)</div>", 
				"<span class=\"rating_nums\">(.*?)</span>",
				"<span class=\"pl\">((.*?)人评价)"
		};
		ArrayList<String> title = getElements(regex[0], context);
		ArrayList<String> info = getElements(regex[1], context);
		ArrayList<String> rating = getElements(regex[2], context);
		ArrayList<String> number = getElements(regex[3], context);
		//ArrayList<String> sqlqueries = new ArrayList<String>();
		for(int i = 0; i<title.size();i++){
			String author = "";
			String insert = "";
			String[] splitinfo = info.get(i).split("/");
			int length = splitinfo.length;
			if(length>=5){
				author = splitinfo[0].trim()+" "+splitinfo[1].trim();
				insert = "'"+ title.get(i)+"',"+getIntergerString(rating.get(i))+","+getIntergerString(number.get(i))+",'"+ author+"','"+splitinfo[2].trim()+"','"+splitinfo[3].trim()+"',"+getIntergerString(splitinfo[4])+",'"+url.split("tag/|\\?start=")[1]+"'";
			}else if(length==4){
				author = splitinfo[0].trim();
				insert = "'"+title.get(i)+"',"+getIntergerString(rating.get(i))+","+getIntergerString(number.get(i))+",'"+ author+"','"+splitinfo[1].trim()+"','"+splitinfo[2].trim()+"',"+getIntergerString(splitinfo[3])+",'"+url.split("tag/|\\?start=")[1]+"'";
			}else{
				insert = "'"+title.get(i)+"',"+getIntergerString(rating.get(i))+","+getIntergerString(number.get(i))+",'"+ info.get(i)+"',' ',' ',"+0+",'"+url.split("tag/|\\?start=")[1]+"'";
			}
			//System.out.println(insert);
			sqlqueries.add(insert);
		}
	}
	
	public String getIntergerString(String s){
		  Pattern pa = Pattern.compile("[^0-9]");
		  Matcher ma = pa.matcher(s);
		  return ma.replaceAll("").trim();
	 }
	
	public ArrayList<String> getElements(String regex, String s){
		  ArrayList<String> list = new ArrayList<String>();
		  Pattern pa = Pattern.compile(regex);
		  Matcher ma = pa.matcher(s);
		  
		  while (ma.find()){
			  String ss = ma.group(1).replaceAll("/r|/n", "").trim();
			  list.add(ss);
		  }
		  return list;
	 }
}