package com.amsscala

import com.amsscala.common.GameProtocol._

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
  def engine(p1Answer: Answer, p2Answer: Answer,
             sanction1: Int = 1, sanction2: Int = 2, sanction3: Int = 3) = {
    (p1Answer, p2Answer) match {
      case (Silent, Silent) => (sanction1, sanction1)
      case (Talk, Silent)   => (0, sanction3)
      case (Silent, Talk)   => (sanction3, 0)
      case (Talk, Talk)     => (sanction2, sanction2)
    }
  }
}