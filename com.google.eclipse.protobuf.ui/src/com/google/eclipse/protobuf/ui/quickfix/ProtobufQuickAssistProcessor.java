/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.ui.quickfix;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.ui.texteditor.spelling.SpellingCorrectionProcessor;
import org.eclipse.xtext.ui.editor.quickfix.XtextQuickAssistProcessor;

import com.google.inject.Inject;

/**
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ProtobufQuickAssistProcessor extends XtextQuickAssistProcessor {
  @Inject private SpellingCorrectionProcessor spellingCorrectionProcessor;

  @Override public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext context) {
    List<ICompletionProposal> proposals = newArrayList();
    proposals.addAll(spellingFixes(context));
    proposals.addAll(newArrayList(super.computeQuickAssistProposals(context)));
    return proposals.toArray(new ICompletionProposal[proposals.size()]);
  }

  private List<ICompletionProposal> spellingFixes(IQuickAssistInvocationContext context) {
    ICompletionProposal[] spellingFixes = spellingCorrectionProcessor.computeQuickAssistProposals(context);
    if (spellingFixes.length == 0) {
      return emptyList();
    }
    if (spellingFixes.length == 1 && isNoCompletionsProposal(spellingFixes[0])) {
      return emptyList();
    }
    return newArrayList(spellingFixes);
  }

  private boolean isNoCompletionsProposal(ICompletionProposal p) {
    return p.getClass().getSimpleName().equals("NoCompletionsProposal");
  }
}