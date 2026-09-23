import sys

path = "app/src/main/java/com/example/api/GooglePlacesApiService.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace(
    'val includedRegionCodes: List<String> = listOf("in"),\n    val includedPrimaryTypes: List<String> = listOf("locality", "sublocality", "administrative_area_level_1", "administrative_area_level_2", "political", "neighborhood")',
    'val includedRegionCodes: List<String> = listOf("in")'
)

with open(path, "w") as f:
    f.write(content)
