/*  QuantizedContourModel.java

    Copyright (c) 2009-2011 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuToBI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuToBI.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.ConditionalDistribution;
import edu.cuny.qc.speech.AuToBI.core.Contour;

/**
 * QuantizedContourModel is a sequential multinomial model of a quantized contour.
 */
public class QuantizedContourModel {
  public ContourQuantizer cq;  // A quantizer.
  public ConditionalDistribution[] time_models;  // The component time aligned models.

  /**
   * Constructs a new QuantizedContourModel.
   *
   * @param cq          a quantizer
   * @param time_models the time aligned models
   */
  public QuantizedContourModel(ContourQuantizer cq, ConditionalDistribution[] time_models) {
    this.cq = cq;
    this.time_models = time_models;
  }

  /**
   * Generates the log likelihood that a contour was generated by this QuantizedContourModel.
   *
   * @param c the contour
   * @return the log likelihood
   * @throws ContourQuantizerException if the contour cannot be quantized.
   */
  public double evaluateContour(Contour c) throws ContourQuantizerException {
    int[] quantized = cq.quantize(c);
    double log_p = 0;

    String prev_value = "";
    for (int i = 0; i < quantized.length; ++i) {
      String value = Integer.toString(quantized[i]);
      if (time_models[i].containsKey(prev_value) && time_models[i].get(prev_value).containsKey(value))
        log_p += Math.log(time_models[i].get(prev_value).get(value));
      else
        log_p = -Double.MAX_VALUE;
      prev_value = value;
    }

    return log_p;
  }
}