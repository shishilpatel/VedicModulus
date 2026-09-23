import glob
import re

files = glob.glob('app/src/main/java/com/example/ui/**/*.kt', recursive=True)
total_updated = 0

for filepath in files:
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    lines = content.splitlines()
    new_lines = []
    file_changed = False
    
    i = 0
    while i < len(lines):
        line = lines[i]
        # Check if line contains ButtonDefaults.buttonColors
        if 'ButtonDefaults.buttonColors' in line:
            # Collect full buttonColors block until closing parenthesis
            block_lines = [line]
            open_parens = line.count('(') - line.count(')')
            j = i + 1
            while open_parens > 0 and j < len(lines):
                block_lines.append(lines[j])
                open_parens += lines[j].count('(') - lines[j].count(')')
                j += 1
            
            block_str = '\n'.join(block_lines)
            if 'MaroonPrimary' in block_str or 'cosmicMaroon' in block_str:
                if 'contentColor' not in block_str:
                    # Insert contentColor = Color.White right after containerColor = MaroonPrimary / cosmicMaroon
                    block_str = re.sub(
                        r'(containerColor\s*=\s*(?:MaroonPrimary|cosmicMaroon))',
                        r'\1, contentColor = Color.White',
                        block_str
                    )
                    file_changed = True
                    total_updated += 1
            
            new_lines.extend(block_str.splitlines())
            i = j
        else:
            new_lines.append(line)
            i += 1
            
    if file_changed:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write('\n'.join(new_lines) + '\n')
        print(f"Updated: {filepath}")

print(f"Total updated buttonColors blocks: {total_updated}")
