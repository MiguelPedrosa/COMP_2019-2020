import java.util.ArrayList;
import java.util.List;

/**
 * This class exists because identifiers in J-- are allowed to be
 * declared as Jasmin keywords. As sugested, we created a method that
 * sanitizes all identifier's names and transforms all keywords
 * into acceptable names for Jasmin. We tried filtering as
 * many names as possible, but Jasmin's documentation does not
 * make it easy to find all of its keywords. 
 */
public class JasminSanitizer {

    private static List<String> JasminKeywords;

    static {
        JasminKeywords = new ArrayList<>();

        /* Instructions */
        JasminKeywords.add("nop");
        JasminKeywords.add("aconst_null");
        JasminKeywords.add("iconst_m1");
        JasminKeywords.add("iconst_0");
        JasminKeywords.add("iconst_1");
        JasminKeywords.add("iconst_2");
        JasminKeywords.add("iconst_3");
        JasminKeywords.add("iconst_4");
        JasminKeywords.add("iconst_5");
        JasminKeywords.add("lconst_0");
        
        JasminKeywords.add("lconst_1");
        JasminKeywords.add("fconst_0");
        JasminKeywords.add("fconst_1");
        JasminKeywords.add("fconst_2");
        JasminKeywords.add("dconst_0");
        JasminKeywords.add("dconst_1");
        JasminKeywords.add("bipush");
        JasminKeywords.add("sipush");
        JasminKeywords.add("ldc");
        JasminKeywords.add("ldc_w");
        
        JasminKeywords.add("ldc2_w");
        JasminKeywords.add("iload");
        JasminKeywords.add("lload");
        JasminKeywords.add("fload");
        JasminKeywords.add("dload");
        JasminKeywords.add("aload");          
        JasminKeywords.add("iload_0");
        JasminKeywords.add("iload_1");
        JasminKeywords.add("iload_2");
        JasminKeywords.add("iload_3");
        
        JasminKeywords.add("lload_0");
        JasminKeywords.add("lload_1");
        JasminKeywords.add("lload_2");
        JasminKeywords.add("lload_3");
        JasminKeywords.add("fload_0");
        JasminKeywords.add("fload_1");
        JasminKeywords.add("fload_2");
        JasminKeywords.add("fload_3");
        JasminKeywords.add("dload_0");
        JasminKeywords.add("dload_1");
        
        JasminKeywords.add("dload_2");
        JasminKeywords.add("dload_3");
        JasminKeywords.add("aload_0");
        JasminKeywords.add("aload_1");
        JasminKeywords.add("aload_2");
        JasminKeywords.add("aload_3");
        JasminKeywords.add("iaload");
        JasminKeywords.add("laload");
        JasminKeywords.add("faload");
        JasminKeywords.add("daload");
        
        JasminKeywords.add("aaload");
        JasminKeywords.add("baload");
        JasminKeywords.add("caload");
        JasminKeywords.add("saload");
        JasminKeywords.add("istore");
        JasminKeywords.add("lstore");
        JasminKeywords.add("fstore");
        JasminKeywords.add("dstore");
        JasminKeywords.add("astore");
        JasminKeywords.add("istore_0");
        
        JasminKeywords.add("istore_1");
        JasminKeywords.add("istore_2");
        JasminKeywords.add("istore_3");
        JasminKeywords.add("lstore_0");
        JasminKeywords.add("lstore_1");
        JasminKeywords.add("lstore_2");
        JasminKeywords.add("lstore_3");
        JasminKeywords.add("fstore_0");
        JasminKeywords.add("fstore_1");
        JasminKeywords.add("fstore_2");
        
        JasminKeywords.add("fstore_3");
        JasminKeywords.add("dstore_0");
        JasminKeywords.add("dstore_1");
        JasminKeywords.add("dstore_2");
        JasminKeywords.add("dstore_3");
        JasminKeywords.add("astore_0");
        JasminKeywords.add("astore_1");
        JasminKeywords.add("astore_2");
        JasminKeywords.add("astore_3");
        JasminKeywords.add("iastore");
        
        JasminKeywords.add("lastore");
        JasminKeywords.add("fastore");
        JasminKeywords.add("dastore");
        JasminKeywords.add("aastore");
        JasminKeywords.add("bastore");
        JasminKeywords.add("castore");
        JasminKeywords.add("sastore");
        JasminKeywords.add("pop");
        JasminKeywords.add("pop2");
        JasminKeywords.add("dup");
        
        JasminKeywords.add("dup_x1");
        JasminKeywords.add("dup_x2");
        JasminKeywords.add("dup2");
        JasminKeywords.add("dup2_x1");
        JasminKeywords.add("dup2_x2");
        JasminKeywords.add("swap");
        JasminKeywords.add("iadd");
        JasminKeywords.add("ladd");
        JasminKeywords.add("fadd");
        JasminKeywords.add("dadd");
        
        JasminKeywords.add("isub");
        JasminKeywords.add("lsub");
        JasminKeywords.add("fsub");
        JasminKeywords.add("dsub");
        JasminKeywords.add("imul");
        JasminKeywords.add("lmul");
        JasminKeywords.add("fmul");
        JasminKeywords.add("dmul");
        JasminKeywords.add("idiv");
        JasminKeywords.add("ldiv");
        
        JasminKeywords.add("fdiv");
        JasminKeywords.add("ddiv");
        JasminKeywords.add("irem");
        JasminKeywords.add("lrem");
        JasminKeywords.add("frem");
        JasminKeywords.add("drem");
        JasminKeywords.add("ineg");
        JasminKeywords.add("lneg");
        JasminKeywords.add("fneg");
        JasminKeywords.add("dneg");
        JasminKeywords.add("ishl");
        
        JasminKeywords.add("lshl");
        JasminKeywords.add("ishr");
        JasminKeywords.add("lshr");
        JasminKeywords.add("iushr");
        JasminKeywords.add("lushr");
        JasminKeywords.add("iand");
        JasminKeywords.add("land");
        JasminKeywords.add("ior");
        JasminKeywords.add("lor");
        JasminKeywords.add("ixor");
        
        JasminKeywords.add("lxor");
        JasminKeywords.add("iinc");
        JasminKeywords.add("i2l");
        JasminKeywords.add("i2f");
        JasminKeywords.add("i2d");
        JasminKeywords.add("l2i");
        JasminKeywords.add("l2f");
        JasminKeywords.add("l2d");
        JasminKeywords.add("f2i");
        JasminKeywords.add("f2l");
        
        JasminKeywords.add("f2d");
        JasminKeywords.add("d2i");
        JasminKeywords.add("d2l");
        JasminKeywords.add("d2f");
        JasminKeywords.add("i2b");
        JasminKeywords.add("i2c");
        JasminKeywords.add("i2s");
        JasminKeywords.add("lcmp");
        JasminKeywords.add("fcmpl");
        JasminKeywords.add("fcmpg");
        
        JasminKeywords.add("dcmpl");
        JasminKeywords.add("dcmpg");
        JasminKeywords.add("ifeq");
        JasminKeywords.add("ifne");
        JasminKeywords.add("iflt");
        JasminKeywords.add("ifge");
        JasminKeywords.add("ifgt");
        JasminKeywords.add("ifle");
        JasminKeywords.add("if_icmpeq");
        JasminKeywords.add("if_icmpne");
        
        JasminKeywords.add("if_icmplt");
        JasminKeywords.add("if_icmpge");
        JasminKeywords.add("if_icmpgt");
        JasminKeywords.add("if_icmple");
        JasminKeywords.add("if_acmpeq");
        JasminKeywords.add("if_acmpne");
        JasminKeywords.add("goto");
        JasminKeywords.add("jsr");
        JasminKeywords.add("ret");
        JasminKeywords.add("tableswitch");
        
        JasminKeywords.add("lookupswitch");
        JasminKeywords.add("ireturn");
        JasminKeywords.add("lreturn");
        JasminKeywords.add("freturn");
        JasminKeywords.add("dreturn");
        JasminKeywords.add("areturn");
        JasminKeywords.add("return");
        JasminKeywords.add("getstatic");
        JasminKeywords.add("putstatic");
        JasminKeywords.add("getfield");
        
        JasminKeywords.add("putfield");
        JasminKeywords.add("invokevirtual");
        JasminKeywords.add("invokespecial");
        JasminKeywords.add("invokestatic");
        JasminKeywords.add("invokeinterface");
        JasminKeywords.add("xxxunusedxxx");
        JasminKeywords.add("new");
        JasminKeywords.add("newarray");
        JasminKeywords.add("anewarray");
        JasminKeywords.add("arraylength");
        
        JasminKeywords.add("athrow");
        JasminKeywords.add("checkcast");
        JasminKeywords.add("instanceof");
        JasminKeywords.add("monitorenter");
        JasminKeywords.add("monitorexit");
        JasminKeywords.add("wide");
        JasminKeywords.add("multianewarray");
        JasminKeywords.add("ifnull");
        JasminKeywords.add("ifnonnull");
        JasminKeywords.add("goto_w");
        
        JasminKeywords.add("jsr_w");
        JasminKeywords.add("breakpoint");

        /** Sintaxe Keywords */
        JasminKeywords.add("catch");
        JasminKeywords.add("class");
        JasminKeywords.add("end"); 
        JasminKeywords.add("field"); 
        JasminKeywords.add("implements"); 
        JasminKeywords.add("interface"); 
        JasminKeywords.add("limit"); 
        JasminKeywords.add("line"); 
    
        JasminKeywords.add("method"); 
        JasminKeywords.add("source"); 
        JasminKeywords.add("super"); 
        JasminKeywords.add("throws"); 
        JasminKeywords.add("var");

        JasminKeywords.add("public");
        JasminKeywords.add("private");
        JasminKeywords.add("protected");
        JasminKeywords.add("static");
        JasminKeywords.add("final");
        JasminKeywords.add("synchronized");
        JasminKeywords.add("volatile");
        JasminKeywords.add("transient");
        JasminKeywords.add("native");
        JasminKeywords.add("interface");
        JasminKeywords.add("abstract");
        JasminKeywords.add("all");
    }
    
    public static String getJasminIdentifier(String identifier) {

        if(JasminKeywords.contains(identifier)) {
            identifier = "!" + identifier;
        }

        return identifier;
    }
}