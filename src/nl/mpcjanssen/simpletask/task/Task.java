/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package nl.mpcjanssen.simpletask.task;

import nl.mpcjanssen.simpletask.Constants;
import nl.mpcjanssen.simpletask.util.RelativeDate;
import nl.mpcjanssen.simpletask.util.Strings;

import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings("serial")
public class Task implements Serializable, Comparable<Task> {
    private static final String COMPLETED = "x ";
    private String originalText;
    private final Priority originalPriority;

    private Priority priority;
    private boolean deleted = false;
    private boolean completed = false;
    private String text;
    private String completionDate;
    private String prependedDate;
    private String relativeAge = "";
    private List<String> contexts;
    private List<String> projects;
    private List<String> phoneNumbers;
    private List<String> mailAddresses;
    private List<URL> links;
    private Date dueDate;
    private SimpleDateFormat formatter;
    private long line;


    public Task(long line, String rawText, Date defaultPrependedDate) {
        this.init(line, rawText, defaultPrependedDate);
        this.originalPriority = priority;
        this.originalText = text;
    }

    public Task(long line, String rawText) {
        this(line, rawText, null);
    }

    public void update(long line, String rawText) {
        this.init(line, rawText, null);
    }

    public void init(long line, String rawText, Date defaultPrependedDate) {
        this.line = line;
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult splitResult = splitter.split(rawText);
        this.priority = splitResult.priority;
        this.text = splitResult.text;
        this.prependedDate = splitResult.prependedDate;
        this.completed = splitResult.completed;
        this.completionDate = splitResult.completedDate;
        this.originalText = rawText;

        this.phoneNumbers = PhoneNumberParser.getInstance().parse(text);
        this.mailAddresses = MailAddressParser.getInstance().parse(text);
        this.links = LinkParser.getInstance().parse(text);
        this.contexts = ContextParser.getInstance().parse(text);
        this.projects = ProjectParser.getInstance().parse(text);
        this.deleted = Strings.isEmptyOrNull(text);
        this.formatter = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        if (defaultPrependedDate != null
                && Strings.isEmptyOrNull(this.prependedDate)) {
            this.prependedDate = formatter.format(defaultPrependedDate);
        }

        if (!Strings.isEmptyOrNull(this.prependedDate)) {
            try {
                Date d = formatter.parse(this.prependedDate);
                this.relativeAge = RelativeDate.getRelativeDate(d);
            } catch (ParseException e) {
                // e.printStackTrace();
            }
        }


        Pattern matchPattern = Pattern
                .compile("\\sdue:(\\d{4}-\\d{2}-\\d{2})");
        Matcher matcher = matchPattern.matcher(this.text);
        if (matcher.find()) {
            try {
                this.dueDate = formatter.parse(matcher.group(1));
            } catch (ParseException e) {
                this.dueDate = null;
            }
        }
    }

    public Priority getOriginalPriority() {
        return originalPriority;
    }

    public Date getDueDate() {
        return this.dueDate;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getText() {
        return text;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Priority getPriority() {
        return priority;
    }

    public List<String> getContexts() {
        return contexts;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setPrependedDate(String date) {
        this.prependedDate = date;
        try {
            Date d = formatter.parse(this.prependedDate);
            this.relativeAge = RelativeDate.getRelativeDate(d);
        } catch (ParseException e) {
            // e.printStackTrace();
        }
    }

    public String getPrependedDate() {
        return prependedDate;
    }

    public String getRelativeAge() {
        return relativeAge;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public List<String> getMailAddresses() {
        return mailAddresses;
    }

    public List<URL> getLinks() {
        return links;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void markComplete(Date date) {
        if (!this.completed) {
            this.completionDate = formatter.format(date);
            this.deleted = false;
            this.completed = true;
        }
    }

    public void markIncomplete() {
        if (this.completed) {
            this.completionDate = "";
            this.completed = false;
        }
    }

    // TODO need a better solution (TaskFormatter?) here
    public String inScreenFormat() {
        StringBuilder sb = new StringBuilder();
        if (this.completed) {
            sb.append(COMPLETED).append(this.completionDate).append(" ");
        }
        if (this.getPriority()!=Priority.NONE) {
            sb.append(this.getPriority().inFileFormat()).append(" ");
        }
        sb.append(this.text);
        return sb.toString();
    }

    public boolean inFuture() {
        if (Strings.isEmptyOrNull(this.getPrependedDate())) {
            return false;
        } else {
            try {
                Date createDate = formatter.parse(this.prependedDate);
                Date now = new Date();
                return createDate.after(now);
            } catch (ParseException e) {
                // e.printStackTrace();
                return false;
            }
        }
    }

    public String inFileFormat() {
        StringBuilder sb = new StringBuilder();
        if (this.completed) {
            sb.append(COMPLETED).append(this.completionDate).append(" ");
            if (!Strings.isEmptyOrNull(this.prependedDate)) {
                sb.append(this.prependedDate).append(" ");
            }
        } else {
            if (priority != Priority.NONE) {
                sb.append(priority.inFileFormat()).append(" ");
            }
            if (!Strings.isEmptyOrNull(this.prependedDate)) {
                sb.append(this.prependedDate).append(" ");
            }
        }
        sb.append(this.text);
        return sb.toString();
    }

    public void copyInto(Task destination) {
        destination.init(0, this.inFileFormat(), null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Task other = (Task) obj;
        return (this.inFileFormat().equals(other.inFileFormat()));
    }

    @Override
    public int hashCode() {
        return this.inFileFormat().hashCode();
    }

    public void initWithFilters(ArrayList<String> ctxts, ArrayList<String> pjs) {
        if ((ctxts != null) && (ctxts.size() == 1)) {
            contexts.clear();
            contexts.add(ctxts.get(0));
        }
        if ((pjs != null) && (pjs.size() == 1)) {
            projects.clear();
            projects.add(pjs.get(0));
        }
    }

	public void append(String string) {
		this.init(line, originalText + " " + string , null);
	}

    @Override
    public int compareTo(Task task) {
        return this.inFileFormat().compareTo(task.inFileFormat());
    }

    public long getLine() {
        return line;
    }
}
