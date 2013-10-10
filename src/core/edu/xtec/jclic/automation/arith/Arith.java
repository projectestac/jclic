/*
 * File    : Arith.java
 * Created : 06-may-2001 18:28
 * By      : fbusquets
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2005 Francesc Busquets & Departament
 * d'Educacio de la Generalitat de Catalunya
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details (see the LICENSE file).
 */

package edu.xtec.jclic.automation.arith;

import edu.xtec.jclic.automation.ActiveBagContentKit;
import edu.xtec.jclic.automation.AutoContentProvider;
import edu.xtec.jclic.boxes.ActiveBagContent;
import edu.xtec.util.JDomUtility;
import edu.xtec.util.ResourceBridge;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Random;

/**
 * <CODE>Arith</CODE> is the first implementation of {@link edu.xtec.jclic.automation.AutoContentProvider}.
 * It is based on the code of ARITH2.DLL, made originally for Clic 3.0. It provides
 * activities with randomly generated menthal arithmetics operations. The operations
 * can be additions, substractions, multiplications or divides. The unknown can be the
 * result of the operation or any of the two operators (in the form A # B = ?, A # ? = C or ? # B = C), or
 * also the operation itself (like A ? B = C).
 * Activities must implement {@link edu.xtec.jclic.automation.ActiveBagContentKit.Compatible}
 * in order to use <CODE>Arith</CODE>.
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.08.28
 */
public class Arith extends AutoContentProvider {
    
    private static final String DLL_TITLE="ARITH2.DLL";
    private static final int ARITHVER=0x02;
    
    protected static final int NMAXLOOPS=60;
    protected static final int NOSORT=0, SORTASC=1, SORTDESC=2;
    protected static final int SUM=1, REST=2, MULT=4, DIV=8, NOPERACIONS=4;
    
    // Math signs:
    // \u00D7 - multiply (cross)
    // \u00B7 - multiply (middle dot)
    // \u00F7 - division  (two points+bar)
    protected static final String[] OPSTR={"+", "-", "\u00D7", ":"};
    protected static final int ABX=1, AXC=2, XBC=4, AXBC=8, CAXB=16, NTIPUSEX=5;
    
    protected static final int INDIF=0, AGB=1, BGA=2;
        
    private static final int RES=-12345;
    private static final int MAX_STR_LEN=100;
    
    private static String S="\u00A0";
    
    protected class Num {
        float vf;
        int   c;
    }
    
    protected class Operacio {
        Num numA=new Num();
        Num numB=new Num();
        Num numR=new Num();
        int op;
    }
    
    protected static final String ID="id", A="A", B="B",
    OPERATIONS="operations", PLUS="plus", MINUS="minus", MULTIPLY="multiply", DIVIDE="divide",
    UNKNOWN="unknown",
    RESULT="result", FIRST="first", LAST="last", OPERAND="operand", INVERSE="inverse",
    FROM="from", TO="to", NOT_CARRY="notCarry", DUPLICATES="duplicates",
    ORDER="order", ASCENDING="ascending", DESCENDING="descending",
    CONDITION="condition", FIRST_BIG="firstBig", LAST_BIG="lastBig";
    
    protected static DecimalFormat[] DF;
    protected static final int WILDCARD_DF=5;
    
    Operator opA, opB;
    boolean use_add, use_subst, use_mult, use_div;
    boolean exp_abx, exp_axc, exp_xbc, exp_axbc, exp_caxb;
    int  resultLimInf, resultLimSup;
    boolean resultCarry, resultNoDup;
    int  resultOrder;
    int  opCond;
    
    private Random random;
    
    /** Creates new Arith */
    public Arith() {
        random=new Random();
        opA=new Operator();
        opB=new Operator();
        exp_abx=true;
        use_add=true;
        resultLimInf=Operator.LIM0;
        resultLimSup=Operator.NOLIM;
        resultOrder=NOSORT;
        opCond=INDIF;
        if(DF==null){
            DF=new DecimalFormat[WILDCARD_DF+1];
            DF[0]=new DecimalFormat("0");
            DF[1]=new DecimalFormat("0.0");
            DF[2]=new DecimalFormat("0.00");
            DF[3]=new DecimalFormat("0.000");
            DF[4]=new DecimalFormat("0.0000");
            DF[WILDCARD_DF]=new DecimalFormat("0000000000");
        }
    }
    
    public DecimalFormat getDF(int index){
        return DF[index<=WILDCARD_DF ? index : WILDCARD_DF-1];
    }
    
    @Override
    public org.jdom.Element getJDomElement() {
        
        org.jdom.Element e=super.getJDomElement();
        org.jdom.Element op=opA.getJDomElement();
        op.setAttribute(ID, A);
        e.addContent(op);
        op=opB.getJDomElement();
        op.setAttribute(ID, B);
        e.addContent(op);
        
        org.jdom.Element eop=new org.jdom.Element(OPERATIONS);
        eop.setAttribute(PLUS, JDomUtility.boolString(use_add));
        eop.setAttribute(MINUS, JDomUtility.boolString(use_subst));
        eop.setAttribute(MULTIPLY, JDomUtility.boolString(use_mult));
        eop.setAttribute(DIVIDE, JDomUtility.boolString(use_div));
        e.addContent(eop);
        
        org.jdom.Element eu=new org.jdom.Element(UNKNOWN);
        eu.setAttribute(RESULT, JDomUtility.boolString(exp_abx));
        eu.setAttribute(FIRST, JDomUtility.boolString(exp_xbc));
        eu.setAttribute(LAST, JDomUtility.boolString(exp_axc));
        eu.setAttribute(OPERAND, JDomUtility.boolString(exp_axbc));
        eu.setAttribute(INVERSE, JDomUtility.boolString(exp_caxb));
        e.addContent(eu);
        
        org.jdom.Element er=new org.jdom.Element(RESULT);
        er.setAttribute(FROM, Operator.LIM_CH[resultLimInf]);
        er.setAttribute(TO, Operator.LIM_CH[resultLimSup]);
        if(resultCarry)
            er.setAttribute(NOT_CARRY, JDomUtility.boolString(resultCarry));
        er.setAttribute(DUPLICATES, JDomUtility.boolString(!resultNoDup));
        if(resultOrder!=NOSORT)
            er.setAttribute(ORDER, resultOrder==SORTASC ? ASCENDING : DESCENDING);
            if(opCond!=INDIF)
                er.setAttribute(CONDITION, opCond==AGB ? FIRST_BIG : LAST_BIG);
                e.addContent(er);
                
                return e;
    }
    
    public void setProperties(org.jdom.Element e, Object aux) throws Exception{
        
        org.jdom.Element child;
        String s;
        Iterator itr = e.getChildren(Operator.ELEMENT_NAME).iterator();
        while (itr.hasNext()){
            child=(org.jdom.Element)(itr.next());
            s=child.getAttributeValue(ID);
            if(A.equals(s))
                opA.setProperties(child, aux);
            else if(B.equals(s))
                opB.setProperties(child, aux);
            else
                throw new IllegalArgumentException("Unknown operator: "+s);
        }
        if((child=e.getChild(OPERATIONS))!=null){
            use_add=JDomUtility.getBoolAttr(child, PLUS, use_add);
            use_subst=JDomUtility.getBoolAttr(child, MINUS, use_subst);
            use_mult=JDomUtility.getBoolAttr(child, MULTIPLY, use_mult);
            use_div=JDomUtility.getBoolAttr(child, DIVIDE, use_div);
        }
        if((child=e.getChild(UNKNOWN))!=null){
            exp_abx=JDomUtility.getBoolAttr(child, RESULT, exp_abx);
            exp_xbc=JDomUtility.getBoolAttr(child, FIRST, exp_xbc);
            exp_axc=JDomUtility.getBoolAttr(child, LAST, exp_axc);
            exp_axbc=JDomUtility.getBoolAttr(child, OPERAND, exp_axbc);
            exp_caxb=JDomUtility.getBoolAttr(child, INVERSE, exp_caxb);
        }
        if((child=e.getChild(RESULT))!=null){
            resultLimInf=JDomUtility.getStrIndexAttr(child, FROM, Operator.LIM_CH, resultLimInf);
            resultLimSup=JDomUtility.getStrIndexAttr(child, TO, Operator.LIM_CH, resultLimSup);
            resultCarry=JDomUtility.getBoolAttr(child, NOT_CARRY, resultCarry);
            resultNoDup=!JDomUtility.getBoolAttr(child, DUPLICATES, !resultNoDup);
            s=child.getAttributeValue(ORDER);
            resultOrder= (s==null ? NOSORT : s.equals(ASCENDING) ? SORTASC : SORTDESC);
            s=child.getAttributeValue(CONDITION);
            opCond = (s==null ? INDIF : s.equals(FIRST_BIG) ? AGB : BGA);
        }
    }
    
    @Override
    public boolean setClic3Properties(byte[] ops) {
        int v;
        int i, lb, hb;
        boolean fromBlank=false;
        int p=0;
        int arithVer;
        
        p=opA.setClic3Properties(ops, p);
        
        p=opB.setClic3Properties(ops, p);
        
        v=ops[p++]&0x7F;
        if(v==0) v=SUM;
        use_add  = (v & SUM)!=0;
        use_subst = (v & REST)!=0;
        use_mult = (v & MULT)!=0;
        use_div  = (v & DIV)!=0;
        
        v=ops[p++]&0x7F;
        if(v==0) v=ABX;
        exp_abx  = (v & ABX)!=0;
        exp_axc  = (v & AXC)!=0;
        exp_xbc  = (v & XBC)!=0;
        exp_axbc = (v & AXBC)!=0;
        exp_caxb = (v & CAXB)!=0;
        
        resultLimInf=((i=ops[p++]&0x7F)==0 ? Operator.LIM0:i);
        resultLimSup=((i=ops[p++]&0x7F)==0 ? Operator.NOLIM:i);
        resultCarry=(ops[p++]&0x1)==1;
        resultNoDup=(ops[p++]&0x1)==1;
        resultOrder=ops[p++]&0x3;
        opCond=ops[p++]&0x3;
        
        if(p<ops.length)
            arithVer=ops[p++]&0x7F;
        else
            arithVer=0;
        
        if(arithVer==0){
            arithVer=ARITHVER;
            if(!opA.fromBlank){
                opA.limInf=Operator.adjustLimVer(opA.limInf);
                opA.limSup=Operator.adjustLimVer(opA.limSup);
                opB.limInf=Operator.adjustLimVer(opB.limInf);
                opB.limSup=Operator.adjustLimVer(opB.limSup);
                resultLimInf=Operator.adjustLimVer(resultLimInf);
                resultLimSup=Operator.adjustLimVer(resultLimSup);
            }
        }
        
        if(arithVer>ARITHVER){
            return false;
        }
        
        return true;
    }
        
    boolean genNum(Num n, Operator op, long limInf2, long limSup2){
        int r, exp, rang;
        long ls, li, k, v;
        boolean resolt;
        
        n.c=op.numDec;
        exp= n.c==0 ? 1 : n.c==1 ? 10 : 100;
        ls=Operator.LIMITS[op.limSup];
        if(limSup2!=RES && limSup2<ls)
            ls=limSup2;
        li=Operator.LIMITS[op.limInf];
        if(limInf2!=RES && limInf2>li)
            li=limInf2;
        
        resolt=false;
        if(op.fromList>0){
            n.vf=(long)op.lst[random.nextInt(op.fromList)];
            resolt=true;
        }
        if(!resolt){
            r=random.nextInt(100);
            if(op.wZero && r<=10){
                n.vf=0; resolt=true;
            }
            else if(op.wOne && r>10 && r<=20){
                n.vf=1;
                resolt=true;
            }
            else if(op.wMinusOne && r>20 && r<=30){
                n.vf=-1;
                resolt=true;
            }
        }
        if(!resolt){
            if(li>ls){
                k=li;
                li=ls;
                ls=k;
            }
            rang=(int)(ls-li+1);
            if(rang<0)
                rang=1;
            v=(long)(random.nextInt(rang)+li)*exp;
            if (exp>1)
                v+=random.nextInt(exp);
            n.vf=((float)v)/exp;
            //resolt=true;
        }
        return true;
    }
    
    boolean genOp(Operacio o){
        int i;
        int[] ops=new int[NOPERACIONS];
        int nops, op;
        long rlinf, rlsup, ri2, rs2;
        float q;
        
        rlinf=Operator.LIMITS[resultLimInf];
        rlsup=Operator.LIMITS[resultLimSup];
        
        nops=0;
        if(use_add)
            ops[nops++]=SUM;
        if(use_subst)
            ops[nops++]=REST;
        if(use_mult)
            ops[nops++]=MULT;
        if(use_div)
            ops[nops++]=DIV;
        
        op=ops[random.nextInt(nops)];
        switch(op){
            case SUM:
                for(i=0; i<NMAXLOOPS; i++){
                    genNum(o.numA, opA, RES, rlsup);
                    ri2= o.numA.vf<rlinf ? rlinf-(long)o.numA.vf:RES;
                    rs2=rlsup-(long)o.numA.vf;
                    switch(opCond){
                        case AGB:
                            if(rs2==RES || rs2>o.numA.vf)
                                rs2=(long)o.numA.vf;
                            break;
                        case BGA:
                            if(ri2==RES || ri2<o.numA.vf)
                                ri2=(long)o.numA.vf;
                            break;
                    }
                    genNum(o.numB, opB, ri2, rs2);
                    o.numR.vf=o.numA.vf+o.numB.vf;
                    if(o.numR.vf>=rlinf && o.numR.vf<=rlsup)
                        break;
                }
                o.numR.c = o.numA.c > o.numB.c ? o.numA.c : o.numB.c;
                o.op=0;
                if(resultCarry && o.numA.vf>0 && o.numB.vf>0){
                    int va, vb;
                    q=o.numR.c==2 ? 100 : o.numR.c==1 ? 10 : 1;
                    char[] bufa=getDF(WILDCARD_DF).format((long)(o.numA.vf*q+0.5)).toCharArray();
                    char[] bufb=getDF(WILDCARD_DF).format((long)(o.numB.vf*q+0.5)).toCharArray();
                    for(i=0; i<10; i++)
                        if(bufa[i]!='0' || bufb[i]!='0')
                            break;
                    for(; i<10; i++){
                        va=bufa[i]-'0';
                        vb=bufb[i]-'0';
                        if(va+vb<10)
                            continue;
                        while(va+vb>9){
                            if(va>vb)
                                va=(va>0 ? random.nextInt(va) : 0);
                            else
                                vb=(vb>0 ? random.nextInt(vb) : 0);
                        }
                        bufa[i]='0';
                        for(int x=0; x<va; x++)
                            bufa[i]++;
                        bufb[i]='0';
                        for(int x=0; x<vb; x++)
                            bufb[i]++;
                    }
                    
                    o.numA.vf=(float)(Long.parseLong(new String(bufa)));
                    o.numB.vf=(float)(Long.parseLong(new String(bufb)));
                    o.numR.vf=(float)(long)(o.numA.vf + o.numB.vf + 0.5);
                    
                    o.numA.vf/=q;
                    o.numB.vf/=q;
                    o.numR.vf/=q;
                }
                break;
                
            case REST:
                for(i=0; i<NMAXLOOPS; i++){
                    genNum(o.numA, opA, rlinf, RES);
                    ri2= o.numA.vf > rlsup ? (long)(o.numA.vf - rlsup) : RES;
                    rs2= (long)(o.numA.vf - rlinf);
                    switch(opCond){
                        case AGB:
                            if(rs2==RES || rs2>o.numA.vf)
                                rs2=(long)o.numA.vf;
                            break;
                        case BGA:
                            if(ri2==RES || ri2<o.numA.vf)
                                ri2=(long)o.numA.vf;
                            break;
                    }
                    genNum(o.numB, opB, ri2, rs2);
                    o.numR.vf=o.numA.vf-o.numB.vf;
                    if(o.numR.vf>=rlinf && o.numR.vf<=rlsup)
                        break;
                }
                o.numR.c = o.numA.c > o.numB.c ? o.numA.c : o.numB.c;
                o.op=1;
                if(resultCarry && o.numA.vf>0 && o.numB.vf>0 && o.numA.vf>=o.numB.vf){
                    int va, vb;
                    q = (o.numR.c==2 ? 100 : (o.numR.c==1 ? 10 : 1));
                    char[] bufa=getDF(WILDCARD_DF).format((long)(o.numA.vf*q+0.5)).toCharArray();
                    char[] bufb=getDF(WILDCARD_DF).format((long)(o.numB.vf*q+0.5)).toCharArray();
                    for(i=0; i<10; i++)
                        if(bufb[i]!='0')
                            break;
                    for(; i<10; i++){
                        va=bufa[i]-'0';
                        vb=bufb[i]-'0';
                        if(va>=vb)
                            continue;
                        vb = (va>0 ? random.nextInt(va) : 0);
                        bufb[i]='0';
                        for(int x=0; x<vb; x++)
                            bufb[i]++;
                    }
                    
                    o.numA.vf=(float)(Long.parseLong(new String(bufa)));
                    o.numB.vf=(float)(Long.parseLong(new String(bufb)));
                    o.numR.vf=(float)(long)(o.numA.vf - o.numB.vf + 0.5);
                    
                    o.numA.vf/=q;
                    o.numB.vf/=q;
                    o.numR.vf/=q;
                }
                break;
                
            case MULT:
                for(i=0; i<NMAXLOOPS; i++){
                    genNum(o.numA, opA, RES, RES);
                    ri2= Operator.LIMITS[opB.limInf];
                    rs2= Operator.LIMITS[opB.limSup];
                    switch(opCond){
                        case AGB:
                            if(rs2>o.numA.vf)
                                rs2=(long)o.numA.vf;
                            break;
                        case BGA:
                            if(ri2<o.numA.vf)
                                ri2=(long)o.numA.vf;
                            break;
                    }
                    genNum(o.numB, opB, ri2, rs2);
                    o.numR.vf=o.numA.vf * o.numB.vf;
                    if(o.numR.vf>=rlinf && o.numR.vf<=rlsup)
                        break;
                }
                o.numR.c = o.numA.c + o.numB.c;
                o.op=2;
                break;
                
            case DIV:
                for(i=0; i<NMAXLOOPS; i++){
                    genNum(o.numA, opA, RES, RES);
                    ri2= Operator.LIMITS[opB.limInf];
                    rs2= Operator.LIMITS[opB.limSup];
                    switch(opCond){
                        case AGB:
                            if(rs2>o.numA.vf)
                                rs2=(long)o.numA.vf;
                            break;
                        case BGA:
                            if(ri2<o.numA.vf)
                                ri2=(long)o.numA.vf;
                            break;
                    }
                    genNum(o.numB, opB, ri2, rs2);
                    if(o.numB.vf!=0
                    && Math.abs(o.numA.vf)>=Math.abs(o.numB.vf)
                    && (o.numR.vf=o.numA.vf/o.numB.vf)>=rlinf
                    && o.numR.vf<=rlsup)
                        break;
                }
                if(o.numB.vf==0)
                    o.numB.vf=1;
                o.numR.vf=o.numA.vf / o.numB.vf;
                i=o.numA.c - o.numB.c;
                q=(float)(Math.pow(10, i));
                o.numA.vf*=q;
                o.numR.vf*=q;
                o.numR.vf=((long)o.numR.vf);
                o.numA.vf=o.numR.vf*o.numB.vf;
                o.numA.vf/=q;
                o.numR.vf/=q;
                o.numR.c = i>0 ? i : 0;
                o.op=3;
                break;
                
            default:
                return false;
        }
        
        return true;
    }
    
    public boolean generateContent(Object kit, ResourceBridge rb) {
        boolean result=false;
        if(kit instanceof ActiveBagContentKit){
            ActiveBagContentKit k=(ActiveBagContentKit)kit;
            result=generateContent(k.nRows, k.nCols, k.content, k.useIds, rb);
        }
        return result;
    }
    
    protected boolean generateContent(int nRows, int nCols, ActiveBagContent[] content, boolean useIds, ResourceBridge rb) {
        if(nRows<=0 || nCols<=0 ||
        content==null || content.length<1 || content[0]==null ||
        rb==null)
            return false;
        
        Operacio o;
        Operacio[] op;
        int i, j, k;
        int[] tipus=new int[NTIPUSEX];
        int numTipus, tipX;
        boolean tipInv;
        String va, vb, vc, operator;
        String [] stra, strb, strc;
        int nColsB=nCols, nRowsB=nRows;
        int nCells=nRows*nCols;
        
        if(nCells<2)
            return false;
        
        int[] ass = null;
        
        numTipus=0;
        if(exp_abx)
            tipus[numTipus++]=ABX;
        if(exp_axc)
            tipus[numTipus++]=AXC;
        if(exp_xbc)
            tipus[numTipus++]=XBC;
        if(exp_axbc)
            tipus[numTipus++]=AXBC;
        if(numTipus==0)
            return false;
        tipInv=exp_caxb;
        
        op=new Operacio[nCells];
        stra=new String[nCells];
        strb=new String[nCells];
        strc=new String[nCells];
        
        for(i=0; i<nCells; i++){
            o=new Operacio();
            for(j=0; j<NMAXLOOPS; j++){
                genOp(o);
                if(resultNoDup){
                    for(k=0; k<i; k++){
                        if(o.numR.vf==op[k].numR.vf) break;
                    }
                    if(k==i) break;
                }
                else break;
            }
            op[i]=o;
        }
        
        if(resultOrder!=0){
            for(i=nCells-1; i>0; i--){
                for(j=0; j<i; j++){
                    if((resultOrder==SORTASC && op[j].numR.vf > op[j+1].numR.vf)
                    || (resultOrder==SORTDESC && op[j].numR.vf < op[j+1].numR.vf)){
                        o=op[j];
                        op[j]=op[j+1];
                        op[j+1]=o;
                    }
                }
            }
        }
        
        for(i=0; i<nCells; i++){
            tipX=tipus[random.nextInt(numTipus)];
            va=getDF(op[0].numA.c).format(op[i].numA.vf);
            vb=getDF(op[0].numB.c).format(op[i].numB.vf);
            vc=getDF(op[0].numR.c).format(op[i].numR.vf);
            operator=OPSTR[op[i].op];
            
            if(tipInv)
                strc[i]=vc + S + "=" + S + va + S + operator + S + vb;
            else
                strc[i]=va + S + operator + S + vb + S + "=" + S + vc;
            
            switch(tipX){
                case AXC:
                    strb[i]=vb;
                    if(tipInv)
                        stra[i]=vc + S + "=" + S + va + S + operator + S + "?";
                    else
                        stra[i]=va + S + operator + S + "?" + S + "=" + S + vc;
                    break;
                    
                case XBC:
                    strb[i]=va;
                    if(tipInv)
                        stra[i]=vc + S + "=" + S + "?" + S + operator + S + vb;
                    else
                        stra[i]="?" + S + operator + S + vb + S + "=" + S + vc;
                    break;
                    
                case AXBC:
                    strb[i]=operator;
                    if(tipInv)
                        stra[i]=vc + S + "=" + S + va + S + "?" + S + vb;
                    else
                        stra[i]=va + S + "?" + S + vb + S + "=" + S + vc;
                    break;
                    
                default:
                    strb[i]=vc;
                    if(tipInv)
                        stra[i]="?" + S + "=" + S + va + S + operator + S + vb;
                    else
                        stra[i]=va + S + operator + S + vb + S + "=";
                    break;
            }
            
        }
        
        if(useIds){
            ass=new int[nCells];
            String[] strbx=new String[nCells];
            k=0;
            for(i=0; i<nCells; i++){
                for(j=0; j<k; j++)
                    if(strb[i].equals(strbx[j]))
                        break;
                if(j==k){
                    strbx[k]=strb[i];
                    ass[i]=k;
                    k++;
                }
                else
                    ass[i]=j;                
            }
            
            strb=new String[k];
            for(i=0; i<k; i++)
                strb[i]=strbx[i];
            
            if(nRowsB*nColsB!=k){
                //boolean distH=nColsB>=nRowsB;
                boolean distH=false;
                switch(k){
                    case 6:
                        nRowsB=distH ? 2 : 3; nColsB=distH ? 3 : 2;
                        break;
                        
                    case 8:
                        nRowsB=distH ? 2 : 4; nColsB=distH ? 4 : 2;
                        break;
                        
                    case 9:
                        nRowsB=3; nColsB=3;
                        break;
                        
                    case 10:
                        nRowsB=distH ? 2 : 5; nColsB=distH ? 5 : 2;
                        break;
                        
                    case 12:
                        nRowsB=distH ? 3 : 4; nColsB=distH ? 4 : 3;
                        break;
                        
                    case 14:
                        nRowsB=distH ? 2 : 7; nColsB= distH ? 7 : 2;
                        break;
                        
                    case 15:
                        nRowsB=distH ? 3 : 5; nColsB=distH ? 3 : 5;
                        break;
                        
                    case 16:
                        nRowsB=4; nColsB=4;
                        break;
                        
                    case 18:
                        nRowsB=distH ? 6 : 3; nColsB=distH ? 3 : 6;
                        break;
                        
                    case 20:
                        nRowsB=distH ? 4 : 5; nColsB=distH ? 5 : 4;
                        break;
                        
                    default:
                        nRowsB=distH ? 1 : k; nColsB=distH ? k : 1;
                        break;
                }
            }
        }
        
        content[0].setTextContent(stra, nCols, nRows);
        if(ass!=null)
            content[0].setIds(ass);
        if(content.length>1 && content[1]!=null){
            content[1].setTextContent(strb, nColsB, nRowsB);
            content[1].getShaper().reset(nColsB, nRowsB);
        }
        if(content.length>2 && content[2]!=null)
            content[2].setTextContent(strc, nCols, nRows);
        
        return true;
    }
    
    public static boolean checkClient(Class cl){
        return ActiveBagContentKit.Compatible.class.isAssignableFrom(cl);
    }        
}
