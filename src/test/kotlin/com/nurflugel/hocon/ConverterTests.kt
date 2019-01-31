package com.nurflugel.hocon

class ConverterTests {
    /*
    
    Test cases:
    
     * conf maps to properties:
     * 
     * one="kkkk"
     * two.three.four = 5
     * 
     * aaaa {
     *    bbbb = 5
     *    cccc = "text"
     *    dddd = true
     * }
     * 
     * 
     * cors = [
     *    "some url"
     *    "another URL"
     *    "3 times a charm"
     *  ]
     * 
     * 
     * //comments
     * cors = [
     *    "some url"
     *    "another URL"
     *    "3 times a charm"
     *  ]
     * 
     * 
     * alpha.omega=false
     * // A comment before some code
     * alpha.beta.gamma=7
     * alpha.beta.delta=5
     * 
     * alpha.omega=false
     * // A comment before some code
     * // 2 comments before some code
     * alpha.beta.gamma=7
     * alpha.beta.delta=5
     * 
     * 
     * 
     * aaaa {
     * // A comment before some code
     *    bbbb {
     *         abab= 5
     *         // A comment before some code
     *         cccc = "text"
     *         dddd = true
     *      }
     *  }
     *  
     *  also - auto quote wrapping
     */
}