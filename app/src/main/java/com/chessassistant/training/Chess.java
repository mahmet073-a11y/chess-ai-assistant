package com.chessassistant.training;

import java.util.ArrayList;
import java.util.List;

public class Chess {
    public static final char[] START = ("rnbqkbnr"+"pppppppp"+"........"+"........"+"........"+"........"+"PPPPPPPP"+"RNBQKBNR").toCharArray();
    public static char[] board = START.clone();
    public static boolean white = true;
    public static String suggestion = "Öneri için dokun";
    public static int selected = -1;
    private static int rights = 15; // 1 white king, 2 white queen, 4 black king, 8 black queen
    private static int ep = -1;
    private static final ArrayList<State> history = new ArrayList<>();
    private static int version = 0;
    static class State { char[] b; boolean w; int c,e; State(char[] b,boolean w,int c,int e){this.b=b.clone();this.w=w;this.c=c;this.e=e;} }
    static class Move {int a,b;char promotion;boolean castle,passant;Move(int a,int b){this.a=a;this.b=b;}}
    static boolean isWhite(char c){return c>='A'&&c<='Z';}
    static boolean isBlack(char c){return c>='a'&&c<='z';}
    static boolean own(char c,boolean w){return c!='.'&&(w?isWhite(c):isBlack(c));}
    static boolean inside(int r,int c){return r>=0&&r<8&&c>=0&&c<8;}
    static int value(char p){switch(Character.toLowerCase(p)){case 'p':return 100;case 'n':return 320;case 'b':return 335;case 'r':return 500;case 'q':return 900;case 'k':return 20000;default:return 0;}}
    public static synchronized void reset(){board=START.clone();white=true;rights=15;ep=-1;history.clear();selected=-1;suggestion="Öneri için dokun";version++;}
    public static synchronized boolean undo(){if(history.isEmpty())return false;State s=history.remove(history.size()-1);board=s.b;white=s.w;rights=s.c;ep=s.e;selected=-1;suggestion="Öneri için dokun";version++;return true;}
    private static boolean attacked(char[] b,int square,boolean byWhite){
        int r=square/8,c=square%8;
        for(int i=0;i<64;i++) {char p=b[i];if(!own(p,byWhite))continue;int sr=i/8,sc=i%8,dr=r-sr,dc=c-sc;char t=Character.toLowerCase(p);
            if(t=='p'&&dr==(byWhite?-1:1)&&Math.abs(dc)==1)return true;
            if(t=='n'&&((Math.abs(dr)==2&&Math.abs(dc)==1)||(Math.abs(dr)==1&&Math.abs(dc)==2)))return true;
            if(t=='k'&&Math.max(Math.abs(dr),Math.abs(dc))==1)return true;
            boolean diag=Math.abs(dr)==Math.abs(dc)&&dr!=0,straight=(dr==0||dc==0)&&i!=square;
            if(!((t=='b'&&diag)||(t=='r'&&straight)||(t=='q'&&(diag||straight))))continue;
            int rr=sr+Integer.compare(r,sr),cc=sc+Integer.compare(c,sc),stepR=Integer.compare(r,sr),stepC=Integer.compare(c,sc);boolean clear=true;
            while(rr!=r||cc!=c){if(b[rr*8+cc]!='.'){clear=false;break;}rr+=stepR;cc+=stepC;}if(clear)return true;
        }return false;
    }
    private static boolean check(char[] b,boolean w){char k=w?'K':'k';for(int i=0;i<64;i++)if(b[i]==k)return attacked(b,i,!w);return true;}
    private static void add(List<Move> out,char[] b,int a,int z,boolean epCapture){if(Character.toLowerCase(b[z])=='k')return;Move m=new Move(a,z);m.passant=epCapture;out.add(m);}
    private static List<Move> pseudo(char[] b,boolean w,int castleRights,int enPassant){
        List<Move> out=new ArrayList<>();
        for(int a=0;a<64;a++){char p=b[a];if(!own(p,w))continue;int r=a/8,c=a%8;char t=Character.toLowerCase(p);
            if(t=='p'){int d=w?-1:1,rr=r+d;
                if(inside(rr,c)&&b[rr*8+c]=='.'){add(out,b,a,rr*8+c,false);int rr2=r+2*d;if(r==(w?6:1)&&b[rr2*8+c]=='.')add(out,b,a,rr2*8+c,false);}
                for(int dc:new int[]{-1,1})if(inside(rr,c+dc)){int z=rr*8+c+dc;if(b[z]!='.'&&!own(b[z],w))add(out,b,a,z,false);else if(z==enPassant&&b[z]=='.'&&b[r*8+c+dc]==(w?'p':'P'))add(out,b,a,z,true);}
            }else if(t=='n'||t=='k'){
                int[][] ds=t=='n'?new int[][]{{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}}:new int[][]{{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
                for(int[] d:ds){int rr=r+d[0],cc=c+d[1];if(inside(rr,cc)&&!own(b[rr*8+cc],w))add(out,b,a,rr*8+cc,false);}
                if(t=='k'&&a==(w?60:4)&&!check(b,w)){
                    int mask=w?1:4;if((castleRights&mask)!=0&&b[a+3]==(w?'R':'r')&&b[a+1]=='.'&&b[a+2]=='.'&&!attacked(b,a+1,!w)&&!attacked(b,a+2,!w)){Move m=new Move(a,a+2);m.castle=true;out.add(m);}
                    mask=w?2:8;if((castleRights&mask)!=0&&b[a-4]==(w?'R':'r')&&b[a-1]=='.'&&b[a-2]=='.'&&b[a-3]=='.'&&!attacked(b,a-1,!w)&&!attacked(b,a-2,!w)){Move m=new Move(a,a-2);m.castle=true;out.add(m);}
                }
            }else{
                int[][] ds=t=='b'?new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}}:t=='r'?new int[][]{{-1,0},{1,0},{0,-1},{0,1}}:new int[][]{{-1,-1},{-1,1},{1,-1},{1,1},{-1,0},{1,0},{0,-1},{0,1}};
                for(int[] d:ds){int rr=r+d[0],cc=c+d[1];while(inside(rr,cc)){int z=rr*8+cc;if(own(b[z],w))break;add(out,b,a,z,false);if(b[z]!='.')break;rr+=d[0];cc+=d[1];}}
            }
        }return out;
    }
    private static State apply(State s,Move m){char[] b=s.b.clone();char piece=b[m.a];b[m.b]=piece;b[m.a]='.';
        if(m.passant)b[m.b+(s.w?8:-8)]='.';
        if(m.castle){int rookFrom=m.b>m.a?m.a+3:m.a-4,rookTo=m.b>m.a?m.a+1:m.a-1;b[rookTo]=b[rookFrom];b[rookFrom]='.';}
        if(piece=='P'&&m.b<8)b[m.b]='Q';if(piece=='p'&&m.b>=56)b[m.b]='q';
        int c=s.c;if(piece=='K')c&=~3;if(piece=='k')c&=~12;
        if(m.a==63||m.b==63)c&=~1;if(m.a==56||m.b==56)c&=~2;if(m.a==7||m.b==7)c&=~4;if(m.a==0||m.b==0)c&=~8;
        int newEp=Character.toLowerCase(piece)=='p'&&Math.abs(m.b-m.a)==16?(m.a+m.b)/2:-1;
        return new State(b,!s.w,c,newEp);
    }
    private static List<Move> legal(State s){List<Move> out=new ArrayList<>();for(Move m:pseudo(s.b,s.w,s.c,s.e))if(!check(apply(s,m).b,s.w))out.add(m);return out;}
    public static synchronized boolean play(int a,int z){State s=new State(board,white,rights,ep);for(Move m:legal(s))if(m.a==a&&m.b==z){history.add(s);State n=apply(s,m);board=n.b;white=n.w;rights=n.c;ep=n.e;selected=-1;suggestion="Öneri için dokun";version++;return true;}return false;}
    private static int evaluate(char[] b){int score=0;for(int i=0;i<64;i++){char p=b[i];if(p=='.')continue;boolean w=isWhite(p);int r=i/8,c=i%8;int center=7-(Math.abs(c-3)+Math.abs(r-3));int positional=center*3;if(Character.toLowerCase(p)=='p')positional+=(w?6-r:r-1)*5;if(Character.toLowerCase(p)=='n')positional+=center*3;score+=(w?1:-1)*(value(p)+positional);}return score;}
    private static int search(State s,int depth,int alpha,int beta){List<Move> moves=legal(s);if(moves.isEmpty())return check(s.b,s.w)?(s.w?-100000-depth:100000+depth):0;if(depth==0)return evaluate(s.b);
        moves.sort((a,b)->Integer.compare(value(s.b[b.b]),value(s.b[a.b])));
        int best=s.w?Integer.MIN_VALUE:Integer.MAX_VALUE;
        for(Move m:moves){int v=search(apply(s,m),depth-1,alpha,beta);if(s.w){best=Math.max(best,v);alpha=Math.max(alpha,best);}else{best=Math.min(best,v);beta=Math.min(beta,best);}if(alpha>=beta)break;}return best;
    }
    public static String recommend(){State s;int startVersion; synchronized(Chess.class){s=new State(board,white,rights,ep);startVersion=version;}
        List<Move> moves=legal(s);if(moves.isEmpty()){String result=check(s.b,s.w)?"Şah mat":"Pat";synchronized(Chess.class){if(version==startVersion)suggestion=result;}return result;}
        moves.sort((a,b)->Integer.compare(value(s.b[b.b]),value(s.b[a.b])));
        int best=s.w?Integer.MIN_VALUE:Integer.MAX_VALUE;Move chosen=moves.get(0);
        for(Move m:moves){int v=search(apply(s,m),2,Integer.MIN_VALUE+1,Integer.MAX_VALUE-1);if((s.w&&v>best)||(!s.w&&v<best)){best=v;chosen=m;}}
        String result=coord(chosen.a)+" → "+coord(chosen.b);synchronized(Chess.class){if(version==startVersion)suggestion=result;}return result;
    }
    static String coord(int i){return "abcdefgh".charAt(i%8)+Integer.toString(8-i/8);}

    public static synchronized String fen() {
        StringBuilder f = new StringBuilder();

        for (int r = 0; r < 8; r++) {
            int empty = 0;
            for (int c = 0; c < 8; c++) {
                char p = board[r * 8 + c];
                if (p == '.') {
                    empty++;
                } else {
                    if (empty > 0) {
                        f.append(empty);
                        empty = 0;
                    }
                    f.append(p);
                }
            }
            if (empty > 0) f.append(empty);
            if (r < 7) f.append('/');
        }

        f.append(white ? " w " : " b ");

        String castle = "";
        if ((rights & 1) != 0) castle += "K";
        if ((rights & 2) != 0) castle += "Q";
        if ((rights & 4) != 0) castle += "k";
        if ((rights & 8) != 0) castle += "q";

        f.append(castle.isEmpty() ? "-" : castle);
        f.append(' ');
        f.append(ep < 0 ? "-" : coord(ep));
        f.append(" 0 1");

        return f.toString();
    }
}
