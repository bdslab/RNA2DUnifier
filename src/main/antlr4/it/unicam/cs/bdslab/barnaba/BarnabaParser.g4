parser grammar BarnabaParser;

options {
    tokenVocab = BarnabaLexer;
}

barnabaFile: info* interaction+ EOF;

info: FILE_NAME
    | sequence;

sequence: sequenceElement+;

sequenceElement : S_IUPAC_CODE S_INT S_INT ;

interaction: residue residue ANNOTATION;

residue: NUCLEOTIDE INT INT;