package it.unicam.cs.bdslab.rna2dunifier;

import it.unicam.cs.bdslab.barnaba.BarnabaLexer;
import it.unicam.cs.bdslab.barnaba.BarnabaParser;
import it.unicam.cs.bdslab.rna2dunifier.listeners.RNApolis.RNApolisParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.RNAview.RNAviewParserCustomListener;
import it.unicam.cs.bdslab.rna2dunifier.listeners.barnaba.BarnabaParserCustomListener;
import it.unicam.cs.bdslab.rnapolis.RNApolisLexer;
import it.unicam.cs.bdslab.rnapolis.RNApolisParser;
import it.unicam.cs.bdslab.rnapolis.RNApolisParserListener;
import it.unicam.cs.bdslab.rnaview.RNAviewLexer;
import it.unicam.cs.bdslab.rnaview.RNAviewParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        /*String input = """
                >strand_A
                seq GGAAGGUUUUUCUUUUCCUGAGGCGAAAGUCUCAGGUUUUGCUUUUUGGCCUUUCUUAAAAAAAAAAAAAGCAAAA
                cWW .[[[[[..........((((((((....))))))))(((((((((((..]]]]]...........)))))))))))
                cWH ......E({BFDA<C[.................................................e)}bfd.a>c]
                cSW ..................................................(............)............
                tSW .....(..........................................................)...........
                tSH ........................(.).................................................
                """;
        CharStream cs = CharStreams.fromString(input);
        RNApolisLexer lexer = new RNApolisLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RNApolisParser parser = new RNApolisParser(tokens);
        ParseTree tree = parser.rnapolisFile(); // parse
        RNApolisParserCustomListener listener = new RNApolisParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        System.out.println(listener.getStructures().toString());
        */


        String input = """
                PDB data file name: /data/preprocessed/2K95_A.pdb_new
                -----------------------------------------------------------
                CRITERIA USED TO GENERATE BASE-PAIR:\s
                  3.40 --> upper H-bond length limits (ON..ON).
                 26.00 --> max. distance between paired base origins.
                  2.50 --> max. vertical distance between paired base origins.
                 65.00 --> max. angle between paired bases [0-90].
                  5.40 --> min. distance between RN9/YN1 atoms.
                  8.00 --> max. distance criterion for helix break[0-12]
                -----------------------------------------------------------
                BASE-PAIR INSTRUCTIONS:\s
                Column 1 is rnaview assigned base numbers n1_n2, start from 1.
                Column 2 & 3 are chain ID & residue number in input PDB file.
                Column 4 is for base pair. The left & right are the bases as\s
                         identified by column 2 & 3 and 5 & 6.
                Column 5 & 6 are residue number & chain ID in input PDB file.
                Column 7 is for base pair annotation. The standard Watson-Crick
                         (W.C.) pairs are annotated as -/- (AU,AT) or +/+ (GC).
                         Other pairs are annotated as Leontis_Westhof Classification.
                         The three edges: W(Watson-Crick); H(Hoogsteen); S(suger).
                         e.g. W/H means the pair is edge of Watson-Crick & Hoogsteen.
                Column 8 is glycosidic bond orientation (either cis or trans).
                         e.g. 'W/H cis' means the pair is interaction on Watson-Crick
                         and Hoogsteen side, glycosidic bond orientation is 'cis'.
                Column 9 corresponds to Saenger Classification.
                
                Other columns:\s
                        Syn sugar-base conformations are annotated as (syn).
                        Stacked base pairs are annotated as (stack).
                        Non-identified edges are annotated as (.) or (?)
                        Tertiary interactions are marked by (!) in the line.
                Reference:
                Yang et al (2003) Nucleic Acids Research, Vol31,No13,p3450-3461.
                -----------------------------------------------------------
                BEGIN_base-pair
                     1_29, A:    93 G-C   121 A: +/+ cis         XIX
                     2_28, A:    94 G-C   120 A: +/+ cis         XIX
                     3_27, A:    95 G-C   119 A: +/+ cis         XIX
                     3_28, A:    95 G-C   120 A:      stacked
                     4_26, A:    96 C-G   118 A: +/+ cis         XIX
                     5_25, A:    97 U-A   117 A: -/- cis         XX
                     5_35, A:    97 U-A   171 A: S/H cis         n/a
                      6_7, A:    98 G-U    99 A: S/S cis         n/a
                     6_24, A:    98 G-C   116 A: +/+ cis         XIX
                     6_36, A:    98 G-A   172 A: S/H tran        XI
                     7_36, A:    99 U-A   172 A:      stacked
                     7_37, A:    99 U-A   173 A: W/H cis         XXIII
                     8_38, A:   100 U-A   174 A: W/H cis         XXIII
                     9_39, A:   101 U-A   175 A: W/H cis         XXIII
                    10_40, A:   102 U-A   176 A: W/H cis         XXIII
                    15_47, A:   107 G-C   183 A: +/+ cis         XIX
                    16_46, A:   108 C-G   182 A: +/+ cis         XIX
                    17_45, A:   109 U-A   181 A: -/- cis         XX
                    18_44, A:   110 G-C   180 A: +/+ cis         XIX
                    19_43, A:   111 A-U   179 A: -/- cis         XX
                    20_42, A:   112 C-G   178 A: +/+ cis         XIX
                    21_40, A:   113 U-A   176 A: -/- cis         XX
                    22_39, A:   114 U-A   175 A: -/- cis         XX
                    23_38, A:   115 U-A   174 A: -/- cis         XX
                    26_33, A:   118 G-A   169 A: S/H tran        XI
                    31_32, A:   167 A-A   168 A:      stacked
                    33_34, A:   169 A-C   170 A:      stacked
                    11_20, A:   103 U-C   112 A: H/W cis         !1H(b_b)
                    12_19, A:   104 C-A   111 A: S/H tran        !1H(b_b)
                    24_37, A:   116 C-A   173 A: S/W cis         !1H(b_b)
                     4_33, A:    96 C-A   169 A: S/H cis         !(s_s)
                    12_14, A:   104 C-C   106 A: S/S tran        !(s_s)
                    14_44, A:   106 C-C   180 A: H/. tran        !(s_s)
                    25_35, A:   117 A-A   171 A: S/W cis         !(s_s)
                    28_32, A:   120 C-A   168 A: S/S cis         !(s_s)
                    29_31, A:   121 C-A   167 A: S/S tran        !(s_s)
                    32_33, A:   168 A-A   169 A: S/H tran        !(s_s)
                    36_37, A:   172 A-A   173 A: S/S cis         !(s_s)
                END_base-pair
                
                Summary of triplets and higher multiplets
                BEGIN_multiplets
                4_26_33_| [1 3]  A: 96 C  +  A: 118 G  +  A: 169 A
                5_25_35_| [2 3]  A: 97 U  +  A: 117 A  +  A: 171 A
                6_7_24_37_| [3 4]  A: 98 G  +  A: 99 U  +  A: 116 C  +  A: 173 A
                8_23_38_| [4 3]  A: 100 U  +  A: 115 U  +  A: 174 A
                9_22_39_| [5 3]  A: 101 U  +  A: 114 U  +  A: 175 A
                10_21_40_| [6 3]  A: 102 U  +  A: 113 U  +  A: 176 A
                6_24_36_37_| [7 4]  A: 98 G  +  A: 116 C  +  A: 172 A  +  A: 173 A
                END_multiplets
                
                  The total base pairs =  23 (from   48 bases)
                ------------------------------------------------
                 Standard  WW--cis  WW-tran  HH--cis  HH-tran  SS--cis  SS-tran
                       15        0        0        0        0        1        0
                  WH--cis  WH-tran  WS--cis  WS-tran  HS--cis  HS-tran
                        4        0        0        0        1        2
                ------------------------------------------------
                """;
        CharStream cs = CharStreams.fromString(input);
        RNAviewLexer lexer = new RNAviewLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RNAviewParser parser = new RNAviewParser(tokens);
        ParseTree tree = parser.rnaviewFile(); // parse
        RNAviewParserCustomListener listener = new RNAviewParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        System.out.println(listener.getStructure().toString());


        String input2 = """
                # ./barnaba/bin/barnaba ANNOTATE --pdb /data/preprocessed/2K95_A.pdb\s
                #RES1       RES2       ANNO\s
                # PDB 2K95_A.pdb\s
                # sequence G_93_0-G_94_0-G_95_0-C_96_0-U_97_0-G_98_0-U_99_0-U_100_0-U_101_0-U_102_0-U_103_0-C_104_0-U_105_0-C_106_0-G_107_0-C_108_0-U_109_0-G_110_0-A_111_0-C_112_0-U_113_0-U_114_0-U_115_0-C_116_0-A_117_0-G_118_0-C_119_0-C_120_0-C_121_0-C_166_0-A_167_0-A_168_0-A_169_0-C_170_0-A_171_0-A_172_0-A_173_0-A_174_0-A_175_0-A_176_0-U_177_0-G_178_0-U_179_0-C_180_0-A_181_0-G_182_0-C_183_0-A_184_0
                G_93_0     C_121_0     WCc\s
                G_94_0     C_120_0     WCc\s
                G_94_0     A_168_0     XXX\s
                G_95_0     C_119_0     WCc\s
                G_95_0     A_168_0     XXX\s
                C_96_0     G_118_0     WCc\s
                U_97_0     A_117_0     WCc\s
                U_97_0     A_171_0     SHc\s
                G_98_0     U_99_0      SHc\s
                G_98_0     C_116_0     WCc\s
                G_98_0     A_172_0     SWc\s
                U_99_0     A_173_0     WHc\s
                U_100_0    A_174_0     WHc\s
                U_101_0    A_175_0     WHc\s
                U_102_0    A_176_0     XXX\s
                U_103_0    G_178_0     XXX\s
                C_104_0    G_110_0     WHc\s
                C_104_0    A_111_0     WHc\s
                C_104_0    U_179_0     XXX\s
                C_104_0    C_180_0     SHc\s
                U_105_0    A_181_0     XXX\s
                U_105_0    C_183_0     XXX\s
                G_107_0    C_183_0     WCc\s
                C_108_0    G_182_0     WCc\s
                U_109_0    A_181_0     WCc\s
                G_110_0    C_180_0     WCc\s
                A_111_0    U_179_0     WCc\s
                C_112_0    G_178_0     WCc\s
                U_113_0    A_176_0     WCc\s
                U_114_0    A_175_0     WCc\s
                U_115_0    A_174_0     WCc\s
                C_116_0    A_173_0     SWc\s
                A_117_0    A_171_0     XXX\s
                G_118_0    A_169_0     SHc\s
                A_172_0    A_173_0     SHc\s
                """;
        CharStream cs2 = CharStreams.fromString(input2);
        BarnabaLexer lexer2 = new BarnabaLexer(cs2);
        CommonTokenStream tokens2 = new CommonTokenStream(lexer2);
        BarnabaParser parser2 = new BarnabaParser(tokens2);
        ParseTree tree2 = parser2.barnabaFile(); // parse
        BarnabaParserCustomListener listener2 = new BarnabaParserCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener2, tree2);
        System.out.println(listener2.getStructure().toString());
    }
}