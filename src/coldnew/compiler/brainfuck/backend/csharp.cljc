(ns coldnew.compiler.brainfuck.backend.csharp
  (:require [clojure.pprint :refer [cl-format]]
            [clojure.string :as str]
            [coldnew.compiler.brainfuck.utils :refer [line-indent]]))

(declare generate-runtime ir->code)

(defn ir->csharp
  ([ir] (ir->csharp ir 3000))
  ([ir num-cells]
   (generate-runtime num-cells (apply str (map ir->code ir)))))

(defn generate-runtime
  [num-cells body]
  (cl-format nil "// This file is generated by coldnew's brainfuck compiler
// You can use following command to execute it:
//
//    mcs xxx.cs
//    mono xxx.exe
//

using System;

public class BrainFuck
{
\tpublic static void Main()
\t{
\tvar cells = new long[~d];
\tvar ptr = 0;

~d
\t}
}
" num-cells body))

(defmulti ir->code (fn [ir] (:op ir)))

(defmethod ir->code :default
  [ir]
  (throw (ex-info "Unknown IR found " {:ir ir})))

;;;; Basic IR

(defmethod ir->code :add
  [ir]
  (line-indent ir "cells[ptr] += 1;"))

(defmethod ir->code :sub
  [ir]
  (line-indent ir "cells[ptr] -= 1;"))

(defmethod ir->code :right
  [ir]
  (line-indent ir "ptr += 1;"))

(defmethod ir->code :left
  [ir]
  (line-indent ir "ptr -= 1;"))

(defmethod ir->code :output
  [ir]
  (line-indent ir "Console.Write((char)cells[ptr]);"))

(defmethod ir->code :input
  [ir]
  (line-indent ir "cells[ptr]= Console.Read();"))

(defmethod ir->code :loop
  [ir]
  (str (line-indent ir "while (cells[ptr] != 0) {")
       (str/join "" (map ir->code (:children ir)))
       (line-indent ir "}")))

;;;; Extended IR (for optimize)

(defmethod ir->code :set-cell-value
  [ir]
  (line-indent ir "cells[ptr] += " (:val ir) ";"))

(defmethod ir->code :set-cell-pointer
  [ir]
  (line-indent ir "ptr += " (:val ir) ";"))

(defmethod ir->code :clear
  [ir]
  (line-indent ir "cells[ptr] = 0;"))
