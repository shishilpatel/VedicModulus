import re

with open("app/src/main/java/com/example/ui/components/PlayBillingPaywallDialog.kt", "r") as f:
    content = f.read()

footer_old = """                Text(
                    text = "Payments and subscriptions are securely processed by Google Play. Auto-renewing plans can be cancelled anytime in Google Play Store > Subscriptions at least 24h before renewal. Lifetime plans are non-consumable and permanently linked to your Google Account.",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )"""

footer_new = """                Text(
                    text = "Payment will be charged to your Google Play account. Subscription automatically renews unless canceled in your Google Play Subscriptions settings. Auto-renewing plans can be cancelled anytime in Google Play Store > Subscriptions at least 24h before renewal. Lifetime plans are non-consumable and permanently linked to your Google Account.",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )"""
                
content = content.replace(footer_old, footer_new)

with open("app/src/main/java/com/example/ui/components/PlayBillingPaywallDialog.kt", "w") as f:
    f.write(content)
