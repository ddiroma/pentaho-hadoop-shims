/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.big.data.impl.shim.mapreduce;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.hadoop.shim.api.internal.mapred.TaskCompletionEvent;
import org.pentaho.hadoop.shim.api.mapreduce.MapReduceService;
import org.pentaho.hadoop.shim.api.internal.mapred.RunningJob;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 12/8/15.
 */
public class RunningJobMapReduceJobAdvancedImplTest {
  private RunningJob runningJob;
  private RunningJobMapReduceJobAdvancedImpl runningJobMapReduceJobAdvanced;
  private MapReduceService.Stoppable stoppable;

  @Before
  public void setup() {
    runningJob = mock( RunningJob.class );
    runningJobMapReduceJobAdvanced = new RunningJobMapReduceJobAdvancedImpl( runningJob );
    stoppable = mock( MapReduceService.Stoppable.class );
  }

  @Test
  public void testKillJob() throws IOException {
    runningJobMapReduceJobAdvanced.killJob();
    verify( runningJob ).killJob();
  }

  @Test( timeout = 500 )
  public void testWaitOnConCompletionStopped() throws IOException, InterruptedException {
    when( stoppable.isStopped() ).thenReturn( true );
    assertFalse( runningJobMapReduceJobAdvanced.waitOnCompletion( 10, TimeUnit.MINUTES, stoppable ) );
  }

  @Test( timeout = 500 )
  public void testWaitOnCompletionFalse() throws IOException, InterruptedException {
    assertFalse( runningJobMapReduceJobAdvanced.waitOnCompletion( 10, TimeUnit.MILLISECONDS, stoppable ) );
  }

  @Test( timeout = 500 )
  public void testWaitOnCompletionCompleteBeforeSleep() throws IOException, InterruptedException {
    when( runningJob.isComplete() ).thenReturn( true );
    assertTrue( runningJobMapReduceJobAdvanced.waitOnCompletion( 10, TimeUnit.MILLISECONDS, stoppable ) );
  }

  @Test( timeout = 500 )
  public void testWaitOnCompletionCompleteAfterSleep() throws IOException, InterruptedException {
    when( runningJob.isComplete() ).thenReturn( false, true );
    assertTrue( runningJobMapReduceJobAdvanced.waitOnCompletion( 10, TimeUnit.MILLISECONDS, stoppable ) );
  }

  @Test
  public void testGetSetupProgress() throws IOException {
    float setupProgress = 1.25f;
    when( runningJob.setupProgress() ).thenReturn( setupProgress );
    assertEquals( setupProgress, runningJobMapReduceJobAdvanced.getSetupProgress(), 0 );
  }

  @Test
  public void testGetMapProgress() throws IOException {
    float mapProgress = 1.25f;
    when( runningJob.mapProgress() ).thenReturn( mapProgress );
    assertEquals( mapProgress, runningJobMapReduceJobAdvanced.getMapProgress(), 0 );
  }

  @Test
  public void testGetReduceProgress() throws IOException {
    float reduceProgress = 1.25f;
    when( runningJob.reduceProgress() ).thenReturn( reduceProgress );
    assertEquals( reduceProgress, runningJobMapReduceJobAdvanced.getReduceProgress(), 0 );
  }

  @Test
  public void testIsSuccessful() throws IOException {
    when( runningJob.isSuccessful() ).thenReturn( true, false );
    assertTrue( runningJobMapReduceJobAdvanced.isSuccessful() );
    assertFalse( runningJobMapReduceJobAdvanced.isSuccessful() );
  }

  @Test
  public void testIsComplete() throws IOException {
    when( runningJob.isComplete() ).thenReturn( true, false );
    assertTrue( runningJobMapReduceJobAdvanced.isComplete() );
    assertFalse( runningJobMapReduceJobAdvanced.isComplete() );
  }

  @Test
  public void testGetTaskCompletionEvents() throws IOException {
    int id = 256;
    TaskCompletionEvent taskCompletionEvent = mock( TaskCompletionEvent.class );
    when( runningJob.getTaskCompletionEvents( 1 ) )
      .thenReturn( new TaskCompletionEvent[] { taskCompletionEvent } );
    when( taskCompletionEvent.getEventId() ).thenReturn( id );
    org.pentaho.hadoop.shim.api.mapreduce.TaskCompletionEvent[] taskCompletionEvents =
      runningJobMapReduceJobAdvanced.getTaskCompletionEvents( 1 );
    assertEquals( 1, taskCompletionEvents.length );
    assertEquals( id, taskCompletionEvents[ 0 ].getEventId() );
  }

  @Test
  public void testGetTaskDiagnostics() throws IOException {
    Object o = new Object();
    String[] value = { "diag" };
    when( runningJob.getTaskDiagnostics( o ) ).thenReturn( value );
    assertArrayEquals( value, runningJobMapReduceJobAdvanced.getTaskDiagnostics( o ) );
  }
}
