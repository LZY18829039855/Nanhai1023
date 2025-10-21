# UTF-8编码导入脚本
$users = Get-Content "src\main\resources\data\test-users.json" -Raw -Encoding UTF8 | ConvertFrom-Json
$successCount = 0
$failCount = 0

foreach ($user in $users) {
    try {
        $params = @{
            username = $user.username
            employeeId = $user.employeeId
            groupType = $user.groupType
            subGroup = $user.subGroup
            userCategory = $user.userCategory
        }
        
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/user/create" -Method Post -Body $params -TimeoutSec 5
        $successCount++
    } catch {
        $failCount++
    }
}

Write-Host "================="
Write-Host "导入完成！"
Write-Host "成功: $successCount"
Write-Host "失败: $failCount"
Write-Host "================="

# 验证
Start-Sleep -Seconds 2
$allUsers = Invoke-RestMethod -Uri "http://localhost:8080/api/user/all"
$aiCount = ($allUsers.data | Where-Object {$_.groupType -eq "AI"}).Count
$nonAiCount = ($allUsers.data | Where-Object {$_.groupType -ne "AI" -and $_.groupType -like "*AI*"}).Count

Write-Host "`n最终验证:"
Write-Host "总用户: $($allUsers.data.Count)"
Write-Host "AI组: $aiCount"
Write-Host "非AI组(包含乱码): $nonAiCount"


