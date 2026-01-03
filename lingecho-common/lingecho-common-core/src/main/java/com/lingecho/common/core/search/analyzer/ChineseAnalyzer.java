package com.lingecho.common.core.search.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * 中文分词器
 * 基于标准分词器，支持中文文本分析
 * 
 * @author heathcetide
 */
public class ChineseAnalyzer extends Analyzer {
    
    private final CharArraySet stopWords;
    
    /**
     * 创建中文分词器
     */
    public ChineseAnalyzer() {
        this(CharArraySet.EMPTY_SET);
    }
    
    /**
     * 创建中文分词器
     * 
     * @param stopWords 停用词集合
     */
    public ChineseAnalyzer(CharArraySet stopWords) {
        this.stopWords = stopWords;
    }
    
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        // 使用标准分词器
        Tokenizer tokenizer = new StandardTokenizer();
        
        // 添加过滤器
        TokenStream tokenStream = new LowerCaseFilter(tokenizer);
        
        // 添加停用词过滤
        if (!stopWords.isEmpty()) {
            tokenStream = new StopFilter(tokenStream, stopWords);
        }
        
        return new TokenStreamComponents(tokenizer, tokenStream);
    }
    
    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
    }
}
