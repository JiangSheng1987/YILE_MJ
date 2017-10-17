package Douniu.data;

import gnu.trove.list.array.TIntArrayList;
import mj.data.poker.douniu.DouniuPai;
import mj.data.poker.douniu.DouniuPaiType;
import mj.net.message.game.douniu.DouniuOnePai;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author zuoge85@gmail.com on 16/10/17.
 */
public class DouniuUtils {

    public static int[] toIndexByDyadicArray(List<DouniuPai[]> dyadicPais) {
        TIntArrayList list = new TIntArrayList();
        for (int i = 0; i < dyadicPais.size(); i++) {
            DouniuPai[] pais = dyadicPais.get(i);
            for (int j = 0; j < pais.length; j++) {
             //   list.add(pais[j].getIndex());
            }
        }
        return list.toArray();
    }
    
   public static int[] toIndex(DouniuPai[] pais) {
    	int[] arr = new int[pais.length];
        int i = 0;
       for (DouniuPai pai : pais) {
           // arr[i] = pai.getIndex();
            i++;
        }
        return arr;
    }

    public static ArrayList toIndex(Collection<DouniuPai> pais) {
    	ArrayList list   = new ArrayList<>();
        int i = 0;
        for (DouniuPai pai : pais) {
           // arr[i] = pai.getIndex();
            i++;
        }
        return list;
    }
 //----------------------------------------------------
    /*
	 * 判断是牛几
	 * 把五张牌的value值都相加，和十求余，得到的值。
	 */
	public static int getValuesModeTen(ArrayList<DouniuPai> shouPai){
		//前提条件是，判断已经有牛的情况下，才能调用这个方法。
		int totalVaules =0 ;
		for(int i =0 ; i< shouPai.size(); i++){
			DouniuPai pai = shouPai.get(i);
			totalVaules += pai.getPokerValue();
		}
		int modeValue = totalVaules%10 ;
		return modeValue ;
	}
	
	
	/*
	 * 判断是否有牛
	 * true :有牛
	 * false ： 无牛
	 */
	public static boolean hasNiu(ArrayList<DouniuPai> shouPai){
		boolean flage=false;
		/*
		 * 先判断，这个里面有value= 10 的牌的个数
		 */
		int totalCount= 0 ; 
		for(int i =0 ; i< shouPai.size(); i++){
			DouniuPai pai = shouPai.get(i);
			if(pai.getPokerValue() == 10 ){
				totalCount ++;
			}
		}
		if(totalCount >= 3 ){
			return true ; 
		}
		
		int modeTenValue =  getValuesModeTen(shouPai);
		
		/*
		 *循环比较其他任意两张牌的mode值是否和modeTenValue相等
		 *如果有直接退出   
		 * */
		for(int i=0 ; i < shouPai.size() - 1 ; i++ ){
			DouniuPai paiPref = shouPai.get(i);
			for(int j = 0 ; j < shouPai.size(); j++){
				DouniuPai paiPostf = shouPai.get(j);
				if((paiPref.getPokerValue() + paiPostf.getPokerValue())%10 == modeTenValue ){
					flage = true;
					break;
				}
			}
			if(flage){
				break;
			}
		}
		return flage;
	}
	
	
	/*
	 * 比花色。
	 */
	public static int compareSuit(ArrayList <DouniuPai>shoupaiSrc, ArrayList <DouniuPai> shouPaiDest){
		int res = 0 ; 
		if(shoupaiSrc != null && shoupaiSrc.size() > 0 
				&&  shouPaiDest != null &&  shouPaiDest.size() > 0 ){
			Collections.sort(shoupaiSrc);
			Collections.sort(shouPaiDest);
			DouniuPai srcPai= shoupaiSrc.get(shoupaiSrc.size() - 1);
			DouniuPai destPai= shoupaiSrc.get(shouPaiDest.size() - 1);
			res = srcPai.getPokerType().compareTo(destPai.getPokerType());
		}
		//加上自定义异常。
		return res; 
	}
	
	/*
	 * 比牛
	 * 如果shoupaiSrc > shouPaiDest  userPlace  1 
	 * shoupaiSrc > shouPaiDest  userPlaceOther返回 -1
	 */
	public static int compareNiu(ArrayList <DouniuPai>shoupaiSrc, ArrayList <DouniuPai> shouPaiDest ){
		/*
		 * 如果牛不一样大的时候  
		 */
		int res = 0 ; 
		int srcShouPaiNiu = getValuesModeTen(shoupaiSrc);
		int destShouPaiNiu = getValuesModeTen(shouPaiDest);
		
		if(srcShouPaiNiu > destShouPaiNiu){
			res = 1 ;
		}else if(srcShouPaiNiu < destShouPaiNiu){
			res = -1 ;
		}else{
			if(srcShouPaiNiu > 0 ){
				res = compareSuit(shoupaiSrc, shouPaiDest);
			}else{
				/*
				 * 如果是牛牛的时候，判断谁的牛大。
				 * 出现的可能性很低。
				 */
			}
		}
		/*
		 * 牛相等的话，比花色
		 */
		return res ;
	}
	
    /*
	 * 判断这个用户是牛几。
	 * 算翻多少倍（一个有牛的用户的牌）
	 */
	public static DouniuPaiType getNiuType(ArrayList<DouniuPai> shouPai){
		DouniuPaiType niuType = DouniuPaiType.WuNiu;
		boolean hasNiu = hasNiu(shouPai);
		if(hasNiu){
			int modeValue = getValuesModeTen(shouPai);
			if(modeValue > 0 ){
				switch(modeValue){
					case 1 : 
						niuType = DouniuPaiType.NiuDing;
						break;
					case 2 : 
						niuType = DouniuPaiType.NiuTwo;
						break;
					case 3 : 
						niuType = DouniuPaiType.NiuThree;
						break;
					case 4 : 
						niuType = DouniuPaiType.NiuFour;
						break;
					case 5 : 
						niuType = DouniuPaiType.NiuFive;
						break;
					case 6 : 
						niuType = DouniuPaiType.NiuSix;
						break;
					case 7 : 
						niuType = DouniuPaiType.NiuSeven;
						break;
					case 8 : 
						niuType = DouniuPaiType.NiuEight;
						break;
					case 9 : 
						niuType = DouniuPaiType.NiuNine;
						break;
				}
			}else{
				/*
				 * 大于或者等于牛牛的。
				 */
				niuType = DouniuPaiType.NiuNiu;
				/*
				 * 判断五小牛
				 */
				int countNum = 0 ;
				for(int i =0 ; i < shouPai.size(); i++){
					DouniuPai pai =shouPai.get(i);
					if(pai.getPokerNum().getNum() < 5){
						countNum++;
					}
				}
				if(countNum == 5 ){
					niuType = DouniuPaiType.NiuFiveSmallNiu;
					return niuType;
				}
				/*
				 * 五花牛
				 */
				countNum = 0 ;
				for(int i =0 ; i < shouPai.size(); i++){
					DouniuPai pai =shouPai.get(i);
					if(pai.getPokerNum().getNum() > 10){
						countNum++;
					}
				}
				
				if(countNum == 5 ){
					niuType = DouniuPaiType.WuHuaNiu;
					return niuType;
				}
				/*
				 * 判断炸牛
				 */
				HashMap<Integer,Integer> paiIndexCount = new HashMap<Integer,Integer>();
				for(int i =0 ; i < shouPai.size(); i++){
					DouniuPai pai =shouPai.get(i);
					Integer key = pai.getPokerNum().getNum();
					Integer value = paiIndexCount.get(key);
					if(value == null){
						value = 1;
					}else{
						value ++;
					}
					paiIndexCount.put(key, value);
				}
				Iterator it = paiIndexCount.keySet().iterator();
				while(it.hasNext()){
					Integer key = (Integer)it.next();
					Integer value = paiIndexCount.get(key);
					if(value >= 4){
						niuType = DouniuPaiType.ZhaDanNiu;
						return niuType;
					}
				}
			}
		}
		return niuType;
	}
	/**
	 * 计算时间的工具
	 */
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String format(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return formatter.format(localDateTime);
    }
    /**
     * 将一个二维数组转化为String
     */
    public static String ConvertString(int[][] list)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.length; i++)
        {	
        	if(i > 0 ){
        		sb.append(",");
        	}
        	sb.append("[");
            for (int j = 0; j < list[i].length; j++)
            {
            	if(j == 0 ){
            		sb.append(list[i][j]);
            	}else{
            		sb.append(","+list[i][j]);
            	}
            }
          
            sb.append("]");
            
        }
        sb.append("]");
        return sb.toString();
    } 
}
