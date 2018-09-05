package com.andrewgilmartin.util;

/**
 * {@code  *
 * value   := number string boolean array hash
 * number  := "-"? [ "0" - "9" ]+
 * string  := "\"" ( [^ "\"", "\\"]+ | ( "\" ( "u" [  "0" - "9", "A" - "F" ]{4} ) | . ) )* "\""
 * boolean := ( "true"  | "false" )
 * array   := "[" ( value ( "," value )* ) "]"
 * hash    := "{" ( entry ( "," entry )* ) "}"
 * entry   := string ":" value
 *
 * }
 */
public class JsonParser {

}
