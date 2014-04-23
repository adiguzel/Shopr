package com.uwetrottmann.shopr.model.explanation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;

import com.adiguzel.shopr.explanation.SurfaceGenerator;
import com.adiguzel.shopr.explanation.model.DimensionArgument;
import com.adiguzel.shopr.explanation.model.Explanation;
import com.adiguzel.shopr.explanation.model.Explanation.Category;
import com.adiguzel.shopr.explanation.model.LocationContext;
import com.uwetrottmann.shopr.R;
import com.uwetrottmann.shopr.algorithm.model.Attributes.Attribute;
import com.uwetrottmann.shopr.algorithm.model.Attributes.AttributeValue;
import com.uwetrottmann.shopr.algorithm.model.ClothingType;
import com.uwetrottmann.shopr.algorithm.model.Color;
import com.uwetrottmann.shopr.algorithm.model.Price;
import com.uwetrottmann.shopr.algorithm.model.Sex;
import com.uwetrottmann.shopr.ui.explanation.ClothingTypePreferenceActivity;
import com.uwetrottmann.shopr.ui.explanation.ColorPreferenceActivity;
import com.uwetrottmann.shopr.ui.explanation.GenderPreferenceActivity;
import com.uwetrottmann.shopr.ui.explanation.ItemListFragmentExplanation;
import com.uwetrottmann.shopr.ui.explanation.PricePreferenceActivity;

public class ShoprSurfaceGenerator implements SurfaceGenerator {

	private List<String> strongArgumentTemplates, weakArgumentTemplates,
			serendipitousityTemplates, contextArgumentTemplates;
	private String supportingArgumentTemplate;
	private String goodAverageTemplate;

	private Context context;
	private Fragment fragment;

	public ShoprSurfaceGenerator(Context context, Fragment fragment) {
		this.context = context;
		this.fragment = fragment;
		initStrongArgumentTemplates(context);
		initWeakArgumentTemplates(context);
		initContextArgumentTemplates(context);
		initSerendipitousityTemplates(context);
		supportingArgumentTemplate = context
				.getString(R.string.explanation_template_on_dimension_weak_supporting_1);
		goodAverageTemplate = context
				.getString(R.string.explanation_template_average_item);
	}

	public CharSequence transform(Explanation explanation) {
		return TextUtils.concat(renderDimensionArguments(explanation),
				renderContextArguments(explanation));
	}

	public CharSequence renderDimensionArguments(Explanation explanation) {
		if (explanation.category() == Category.BY_STRONG_ARGUMENTS) {
			String template = chooseRandomTemplate(strongArgumentTemplates);
			return render(template, explanation.primaryArguments());
		} else if (explanation.category() == Category.BY_WEAK_ARGUMENTS) {
			String template = chooseRandomTemplate(weakArgumentTemplates);
			CharSequence primaryPart = render(template,
					explanation.primaryArguments());
			CharSequence supportingPart = "";
			if(explanation.hasSupportingArguments())
			  supportingPart = render(supportingArgumentTemplate,
					explanation.supportingArguments());
			return TextUtils.concat(primaryPart, supportingPart);
		} else if (explanation.category() == Category.BY_SERENDIPITOUSITY) {
			return chooseRandomTemplate(serendipitousityTemplates);
		} else if (explanation.category() == Category.BY_GOOD_AVERAGE) {
			return goodAverageTemplate;
		} else
			return "";
	}

	public CharSequence renderContextArguments(Explanation explanation) {
		if (explanation.hasContextArguments()) {
			String template = chooseRandomTemplate(contextArgumentTemplates);
			LocationContext locContext = (LocationContext) explanation
					.contextArguments().iterator().next().context();
			String exp = String.format(template,
					locContext.distanceToUserInMeters(explanation.item()));
			return Html.fromHtml(exp);
		} else
			return "";
	}

	private CharSequence render(String template,
			Collection<DimensionArgument> arguments) {
		String text = String.format(template, textify(arguments));
		return renderSpannable(text, arguments);
	}

	private CharSequence renderSpannable(String text,
			Collection<DimensionArgument> dimensionArguments) {
		SpannableString ss = new SpannableString(text);
		for (DimensionArgument dimensionArg : dimensionArguments) {
			final Attribute attr = dimensionArg.dimension().attribute();
			setSpanOnLink(ss, textOf(attr.currentValue()),
					new ShoprClickableSpan() {
						@Override
						public void onClick(View widget) {
							Class<?> cls = null;
							if (attr instanceof Color) {
								cls = ColorPreferenceActivity.class;
							} else if (attr instanceof ClothingType) {
								cls = ClothingTypePreferenceActivity.class;
							} else if (attr instanceof Price) {
								cls = PricePreferenceActivity.class;
							} else if (attr instanceof Sex) {
								cls = GenderPreferenceActivity.class;
							}
							Intent intent = new Intent(context, cls);
							fragment.startActivityForResult(intent,
									ItemListFragmentExplanation.REQUEST_CODE);
						}
					});
		}
		return ss;
	}

	private String textOf(AttributeValue value) {
		return value.descriptor().toLowerCase(Locale.ENGLISH);
	}

	private String textify(Collection<DimensionArgument> arguments) {
		Iterator<DimensionArgument> iterator = arguments.iterator();

		List<String> argumentValues = new ArrayList<String>();
		while (iterator.hasNext()) {
			DimensionArgument arg = iterator.next();
			argumentValues.add(textOf(arg.dimension().attribute()
					.getCurrentValue()));
		}
		return textify(argumentValues);
	}

	private String textify(List<String> elements) {
		String and = context.getString(R.string.conjunction_and);

		if (elements.size() == 0) {
			return "";
		} else if (elements.size() == 1) {
			return elements.get(0);
		} else if (elements.size() == 2) {
			String first = elements.get(0);
			String second = elements.get(1);
			return first + and + second;
		} else {
			String last = elements.get(elements.size() - 1);
			List<String> headList = elements.subList(0, elements.size() - 1);
			return commaSeperated(headList) + and + last;
		}
	}

	private String commaSeperated(List<String> elements) {
		String comma = context.getString(R.string.conjunction_comma);

		if (elements.size() == 0) {
			return "";
		} else if (elements.size() == 1) {
			return elements.get(0);
		} else if (elements.size() == 2) {
			String first = elements.get(0);
			String second = elements.get(1);
			return first + comma + second;
		} else {
			String last = elements.get(elements.size() - 1);
			List<String> headList = elements.subList(0, elements.size() - 1);
			return commaSeperated(headList) + comma + last;
		}
	}

	private void setSpanOnLink(SpannableString ss, String link, ClickableSpan cs) {
		String text = ss.toString();
		int start = text.indexOf(link);
		int end = start + link.length();
		ss.setSpan(cs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	private String chooseRandomTemplate(List<String> templates) {
		return templates.get(new Random().nextInt(templates.size()));
	}

	private List<String> initStrongArgumentTemplates(Context context) {
		strongArgumentTemplates = new ArrayList<String>();
		strongArgumentTemplates
				.add(context
						.getString(R.string.explanation_template_on_dimension_strong_1));
		strongArgumentTemplates
				.add(context
						.getString(R.string.explanation_template_on_dimension_strong_2));
		return strongArgumentTemplates;
	}

	private List<String> initWeakArgumentTemplates(Context context) {
		weakArgumentTemplates = new ArrayList<String>();
		weakArgumentTemplates.add(context
				.getString(R.string.explanation_template_on_dimension_weak_1));
		weakArgumentTemplates.add(context
				.getString(R.string.explanation_template_on_dimension_weak_2));
		return weakArgumentTemplates;
	}

	private List<String> initContextArgumentTemplates(Context context) {
		contextArgumentTemplates = new ArrayList<String>();
		contextArgumentTemplates.add(context
				.getString(R.string.explanation_template_context_location));
		contextArgumentTemplates
				.add(context
						.getString(R.string.explanation_template_context_location_average));
		return contextArgumentTemplates;
	}

	private List<String> initSerendipitousityTemplates(Context context) {
		serendipitousityTemplates = new ArrayList<String>();
		serendipitousityTemplates.add(context
				.getString(R.string.explanation_template_serendipidity_1));
		serendipitousityTemplates.add(context
				.getString(R.string.explanation_template_serendipidity_2));
		serendipitousityTemplates.add(context
				.getString(R.string.explanation_template_serendipidity_3));
		return serendipitousityTemplates;
	}

	abstract class ShoprClickableSpan extends ClickableSpan {

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(android.graphics.Color.BLUE);
			ds.setTypeface(Typeface.DEFAULT_BOLD);
			ds.setUnderlineText(false);
		}
	}
}