package com.chessassistant.training;

import java.util.ArrayList;
import java.util.List;

public class Chess {
    public static final char[] START = (
            "rnbqkbnr"+
            "pppppppp"+
            "........"+
            "........"+
            "........"+
            "........"+
            "PPPPPPPP"+
            "RNBQKBNR").toCharArray();
    public static char[] board = START.clone();
    public static boolean white = true;
    public static String suggestion = "Öneri için dokun";
    public static int selected = -1;
    public static void reset(){ board=START.clone();white=true;selected=-1;suggestion="Öneri için dokun"; }
    static boolean isWhite(char c){return c>='A'&&c<='Z';}
    static boolean isBlack(char c){return c>='a'&&c<='z';}
    static boolean own(char c, boolean w){return c!='.'&&(w?isWhite(c):isBlack(c));}
    static int value(char p){switch(Character.toLowerCase(p)){case 'p':return 100;case 'n':return 320;case 'b':return 330;case 'r':return 500;case 'q':return 900;case 'k':return 20000;default:return 0;}}
    static boolean inside(int r,int c){return r>=0&&r<8&&c>=0&&c<8;}
    static class Move {int a,b;Move(int a,int b){this.a=a;this.b=b;}}
    static List<Move> pseudo(char[] b, boolean w){
        List<Move> out=new ArrayList<>();
        for(int a=0;a<64;a++){
            char p=b[a];if(!own(p,w))continue;
            int r=a/8,c=a%8;char t=Character.toLowerCase(p);
            if(t=='p'){
                int d=w?-1:1,rr=r+d;
                if(inside(rr,c)&&b[rr*8+c]=='.'){
                    out.add(new Move(a,rr*8+c));int start=w?6:1,rr2=r+2*d;
                    if(r==start&&b[rr2*8+c]=='.')out.add(new Move(a,rr2*8+c));
                }
                for(int dc:new int[]{-1,1})if(inside(rr,c+dc)&&b[rr*8+c+dc]!='.'&&!own(b[rr*8+c+dc],w)&&Character.toLowerCase(b[rr*8+c+dc])!='k')out.add(new Move(a,rr*8+c+dc));
            }else if(t=='n'||t=='k'){
                int[][] ds=t=='n'?new int[][]{{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}}:new int[][]{{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
                for(int[] d:ds){int rr=r+d[0],cc=c+d[1];if(inside(rr,cc)&&!own(b[rr*8+cc],w)&&Character.toLowerCase(b[rr*8+cc])!='k')out.add(new Move(a,rr*8+cc));}
            }else{
                int[][] ds=t=='b'?new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}}:t=='r'?new int[][]{{-1,0},{1,0},{0,-1},{0,1}}:new int[][]{{-1,-1},{-1,1},{1,-1},{1,1},{-1,0},{1,0},{0,-1},{0,1}};
                for(int[] d:ds){int rr=r+d[0],cc=c+d[1];while(inside(rr,cc)){
                    int z=rr*8+cc;if(own(b[z],w))break;
                    if(Character.toLowerCase(b[z])!='k')out.add(new Move(a,z));
                    if(b[z]!='.')break;rr+=d[0];cc+=d[1];
                }}
            }
        }return out;
    }
    static char[] apply(char[] b,Move m){char[] x=b.clone();x[m.b]=x[m.a];x[m.a]='.';if(x[m.b]=='P'&&m.b<8)x[m.b]='Q';if(x[m.b]=='p'&&m.b>=56)x[m.b]='q';return x;}
    static boolean attacked(char[] b,int square,boolean byWhite){
        int r=square/8,c=square%8;
        for(int i=0;i<64;i++){
            char p=b[i];if(!own(p,byWhite))continue;int sr=i/8,sc=i%8,dr=r-sr,dc=c-sc;char t=Character.toLowerCase(p);
            if(t=='p'&&dr==(byWhite?-1:1)&&Math.abs(dc)==1)return true;
            if(t=='n'&&((Math.abs(dr)==2&&Math.abs(dc)==1)||(Math.abs(dr)==1&&Math.abs(dc)==2)))return true;
            if(t=='k'&&Math.max(Math.abs(dr),Math.abs(dc))==1)return true;
            boolean diag=Math.abs(dr)==Math.abs(dc)&&dr!=0,straight=(dr==0||dc==0)&&i!=square;
            if(!((t=='b'&&diag)||(t=='r'&&straight)||(t=='q'&&(diag||straight))))continue;
            int stepr=Integer.compare(r,sr),stepc=Integer.compare(c,sc),rr=sr+stepr,cc=sc+stepc;boolean clear=true;
            while(rr!=r||cc!=c){if(b[rr*8+cc]!='.'){clear=false;break;}rr+=stepr;cc+=stepc;}
            if(clear)return true;
        }return false;
    }
    static boolean check(char[] b,boolean w){char k=w?'K':'k';for(int i=0;i<64;i++)if(b[i]==k)return attacked(b,i,!w);return true;}
    static List<Move> legal(char[] b,boolean w){List<Move> out=new ArrayList<>();for(Move m:pseudo(b,w))if(!check(apply(b,m),w))out.add(m);return out;}
    public static boolean play(int a,int z){for(Move m:legal(board,white))if(m.a==a&&m.b==z){board=apply(board,m);white=!white;suggestion="Öneri için dokun";return true;}return false;}
    static int eval(char[] b){int v=0;for(char p:b)if(p!='.')v+=(isWhite(p)?1:-1)*value(p);return v;}
    static int search(char[] b,boolean w,int depth,int alpha,int beta){
        List<Move> moves=legal(b,w);
        if(moves.isEmpty())return check(b,w)?(w?-100000:100000):0;
        if(depth==0)return eval(b);
        int best=w?Integer.MIN_VALUE:Integer.MAX_VALUE;
        for(Move m:moves){int v=search(apply(b,m),!w,depth-1,alpha,beta);if(w){best=Math.max(best,v);alpha=Math.max(alpha,best);}else{best=Math.min(best,v);beta=Math.min(beta,best);}if(beta<=alpha)break;}
        return best;
    }
    public static String recommend(){
        List<Move> moves=legal(board,white);if(moves.isEmpty()){suggestion=check(board,white)?"Şah mat":"Pat";return suggestion;}
        int best=white?Integer.MIN_VALUE:Integer.MAX_VALUE;Move chosen=moves.get(0);
        for(Move m:moves){int v=search(apply(board,m),!white,2,Integer.MIN_VALUE+1,Integer.MAX_VALUE-1);if((white&&v>best)||(!white&&v<best)){best=v;chosen=m;}}
        suggestion=coord(chosen.a)+" → "+coord(chosen.b);return suggestion;
    }
    static String coord(int i){return "abcdefgh".charAt(i%8)+Integer.toString(8-i/8);}
}
