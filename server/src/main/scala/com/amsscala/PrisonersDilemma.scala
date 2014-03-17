package com.amsscala

object PrisonersDilemma {
  /**
   * Each prisoner is given the opportunity either to betray the other, by testifying that the other
   * committed the crime, or to cooperate with the other by remaining silent. Here's how it goes:
   * -If A and B both betray the other, each of them serves 2 years in prison
   * -If A betrays B but B remains silent, A will be set free and B will serve 3 years in prison
   *  (and vice versa)
   * -If A and B both remain silent, both of them will only serve 1 year in prison
   *  (on the lesser charge)
   */
  def engine(Pris1Defects: Boolean, Pris2Defects: Boolean,
             sanction1: Int = 1, sanction2: Int = 2, sanction3: Int = 3) = {
    (Pris1Defects, Pris2Defects) match {
      case (false, false) => (sanction1, sanction1)
      case (true, false)  => (0, sanction3)
      case (false, true)  => (sanction3, 0)
      case (true, true)   => (sanction2, sanction2)
    }
  }
}