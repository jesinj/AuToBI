/*  ContextFrame.java

    Copyright (c) 2009-2010 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.core;

import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

/**
 * ContextFrame is used to slide a word based frame across a list of doubles (like a pitch or intensity contour).
 * <p/>
 * The frame is set at the start of a list of words and is incremented one word at a time.  At each points statistics
 * about the contour can be queried.
 */
@SuppressWarnings("unchecked")
public class ContextFrame {
  protected List<Word> data;           // the word regions
  protected LinkedList<Double> window; // the windowed contour
  protected String feature_name;       // the feature that is analyzed
  private Integer back;                // the amount of back context
  private Integer front;               // the amount of forward context
  private Integer current;             // current point in the word regions
  private Aggregation agg;             // stores the aggregate values

  /**
   * Constructs an empty ContextFrame
   */
  public ContextFrame() {
  }

  /**
   * Constructs a ContextFrame
   *
   * @param data         The words to analyze
   * @param feature_name The feature containing the contour information
   * @param back         the amount of back context
   * @param front        the amount of forward context
   */
  public ContextFrame(List<Word> data, String feature_name, Integer back, Integer front) {
    this.back = back;
    this.front = front;
    this.feature_name = feature_name;
    this.data = data;
    this.current = 0;
    this.agg = new Aggregation();
    init();
  }

  /**
   * Initialize the context frame.
   * <p/>
   * This sets intermediate values and initializes the windowed contour list.
   */
  public void init() {

    window = new LinkedList<Double>();
    for (int i = current; i < Math.min(data.size(), current + front + 1); ++i) {
      // only include data read from the same file.
      if (data.get(i).getAttribute(feature_name) instanceof Double) {
        Double d = (Double) data.get(i).getAttribute(feature_name);
        window.add(d);
        agg.insert(d);
      } else {
        if ((data.get(i).getAttribute(feature_name) instanceof ArrayList) &&
          (((ArrayList) data.get(i).getAttribute(feature_name)).size() > 0)) {
          if (((ArrayList) data.get(i).getAttribute(feature_name)).size() == 0) continue;

          if (((ArrayList) data.get(i).getAttribute(feature_name)).get(0) instanceof TimeValuePair) {
            for (TimeValuePair tvp : (List<TimeValuePair>) data.get(i).getAttribute(feature_name)) {
              Double d = tvp.getValue();

              window.add(d);
              agg.insert(d);
            }
          }
        }
      }
    }
  }

  /**
   * Slides the window forward one region.
   *
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException if a region does not have an attribute associated with feature_name
   */
  public void increment() throws AuToBIException {
    current++;

    if (current > data.size() - 1) {
      window.clear();
      agg = new Aggregation();
      return;
    }

    if (data.get(0).getAttribute(feature_name) instanceof Double) {// Remove trailing value
      if (window.size() > 0) {
        Double d = window.removeFirst();
        agg.remove(d);
      }

      // Add van value
      if (current + front < data.size()) {
        Double d = (Double) data.get(current + front).getAttribute(feature_name);
        window.add(d);
        agg.insert(d);
      }
    } else if (data.get(0).getAttribute(feature_name) instanceof List) {
      // remove trailing values
      Integer points_to_remove = 0;
      if (current - back - 1 > 0 && data.get(current - back - 1).getAttribute(feature_name) instanceof List) {
        points_to_remove = ((List) data.get(current - back - 1).getAttribute(feature_name)).size();
      }
      for (int i = 0; i < Math.min(window.size(), points_to_remove); ++i) {
        Double d = window.removeFirst();
        agg.remove(d);
      }

      // add van values
      if (current + front < data.size()) {
        if (data.get(current + front).getAttribute(feature_name) == null) {
          AuToBIUtils.debug("null feature: " + feature_name);
        }
        for (TimeValuePair tvp : (List<TimeValuePair>) data.get(current + front).getAttribute(feature_name)) {
          Double d = tvp.getValue();

          window.add(d);
          agg.insert(d);
        }
      }
    }
  }

  /**
   * Returns the maximum value in the context frame
   * <p/>
   * Note: this could be made more efficient by tracking the maximum value when it is added to the window
   *
   * @return the maximum value
   */
  public Double getMax() {
    if (agg.getMax() == null) {
      Double max = -(Double.MAX_VALUE);

      for (Double d : window)
        max = Math.max(d, max);
      agg.setMax(max);
    }

    return agg.getMax();
  }

  /**
   * Returns the minimum value in the window.
   * <p/>
   * Note: this could be made more efficient by tracking the minimum value when it is added to the window
   *
   * @return the minimum value
   */
  public Double getMin() {
    if (agg.getMin() == null) {
      Double min = Double.MAX_VALUE;

      for (Double d : window)
        min = Math.min(d, min);
      agg.setMin(min);
    }

    return agg.getMin();
  }

  /**
   * Calculates the mean of the window.
   *
   * @return the mean value
   */
  public Double getMean() {
    return agg.getMean();
  }

  /**
   * Calculates the standard deviation of the window.
   *
   * @return the standard deviation.
   */
  public Double getStdev() {
    return agg.getStdev();
  }

  /**
   * Calculates the size of the window
   *
   * @return the size of the window.
   */
  public int getSize() {
    return agg.getSize();
  }
}