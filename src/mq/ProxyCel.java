package mq;

import db.PageEntity;

public class ProxyCel{
	public String url_hash;
	public String last_modified_time;
	public String last_fetch_time;
	public int weight;
	public String headers;
	public String url;
	public ProxyCel(String url_hash,String lmtime,int weight)
	{
		this.url_hash=url_hash;
		this.last_modified_time=lmtime;
		this.weight=weight;
	}
	public ProxyCel(String url_hash)
	{
		this(url_hash,"",1);
	}
	public ProxyCel(){this("","",1);}
	
	public static ProxyCel parse(PageEntity pe)
	{
		ProxyCel  pc = new ProxyCel(pe.url_hash,pe.last_modified_time,1);
		pc.last_fetch_time=pe.last_fetch_time;
		pc.url=pe.url;
		pc.headers=pe.headers;
		
		return pc;
	}
	
	public boolean equals(Object o)
	{
	   //如果和自身比较，返回TRUE
	   if(this==o) return true;
	   //如果不是这个类的实例，返回FALSE
	   if(!(o instanceof ProxyCel)) return false;
	   
	   final ProxyCel p=(ProxyCel)o;
	   if(this.url_hash.equals(p.url_hash))
	      return true;
	   else
	      return false;
	}
	@Override
	public String toString() {
		return "ProxyCel [url_hash=" + url_hash + ", last_modified_time="
				+ last_modified_time + ", last_fetch_time=" + last_fetch_time
				+ ", weight=" + weight + ", headers=" + headers + ", url="
				+ url + "]";
	}
	
	

}

