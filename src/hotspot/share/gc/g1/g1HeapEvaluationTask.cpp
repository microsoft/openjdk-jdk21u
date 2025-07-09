/*
 * Copyright (c) 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#include "precompiled.hpp"
#include "gc/g1/g1CollectedHeap.hpp"
#include "gc/g1/g1CollectedHeap.inline.hpp"
#include "gc/g1/g1HeapEvaluationTask.hpp"
#include "gc/g1/g1HeapSizingPolicy.hpp"
#include "gc/shared/gc_globals.hpp"
#include "logging/log.hpp"
#include "memory/resourceArea.hpp"
#include "runtime/globals.hpp"
#include "utilities/debug.hpp"
#include "utilities/globalDefinitions.hpp"

G1HeapEvaluationTask::G1HeapEvaluationTask(G1CollectedHeap* g1h, G1HeapSizingPolicy* heap_sizing_policy) :
  G1ServiceTask("G1 Heap Evaluation Task"),
  _g1h(g1h),
  _heap_sizing_policy(heap_sizing_policy) {
}

void G1HeapEvaluationTask::execute() {
  log_debug(gc, sizing)("Starting heap evaluation");

  if (!G1UseTimeBasedHeapSizing) {
    // Feature is disabled, don't reschedule
    return;
  }

  // Ensure we're not running during GC activity
  if (_g1h->is_stw_gc_active()) {
    log_trace(gc, sizing)("GC active, skipping time-based evaluation");
    // Still need to reschedule to keep the periodic task running
    schedule(G1TimeBasedEvaluationIntervalMillis);
    return;
  }

  ResourceMark rm; // Ensure temporary resources are released

  bool should_expand = false;
  size_t resize_amount = _heap_sizing_policy->evaluate_heap_resize(should_expand);
  
  if (resize_amount > 0) {
    // Time-based evaluation only handles uncommit/shrinking, never expansion
    if (should_expand) {
      log_warning(gc, sizing)("Time-based evaluation unexpected expansion request ignored (resize_amount=%zuB)", resize_amount);
      // This should not happen since time-based policy only handles uncommit
      assert(false, "Time-based heap sizing should never request expansion");
    } else {
      log_info(gc, sizing)("Time-based evaluation: shrinking heap by %zuMB", resize_amount / M);
      log_debug(gc, sizing)("Time-based evaluation recommends shrinking by %zuB", resize_amount); 
      _g1h->request_heap_shrink(resize_amount);
    }
  } else {
    // Periodic info log for ongoing evaluation activity (less frequent)
    static int evaluation_count = 0;
    if (++evaluation_count % 10 == 0) { // Log every 10th evaluation when no action taken
      log_info(gc, sizing)("Time-based evaluation: no heap uncommit needed (evaluation #%d)", evaluation_count);
    }
  }

  // Schedule the next evaluation to maintain periodic behavior
  schedule(G1TimeBasedEvaluationIntervalMillis);
}
