with open("app/src/main/java/com/example/ui/components/VedicRealtimeSpashtaCard.kt", "r") as f:
    content = f.read()

content = content.replace("report.calculationEngine", "report!!.calculationEngine")
content = content.replace("report.ayanamshaOffset.toString()", "report!!.ayanamshaOffset.toString()")
content = content.replace("report.ayanamshaName", "report!!.ayanamshaName")
content = content.replace("report.grahas", "report!!.grahas")

with open("app/src/main/java/com/example/ui/components/VedicRealtimeSpashtaCard.kt", "w") as f:
    f.write(content)
